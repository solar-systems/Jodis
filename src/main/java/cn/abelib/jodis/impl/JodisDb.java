package cn.abelib.jodis.impl;

import cn.abelib.jodis.log.AofWriter;
import cn.abelib.jodis.log.JdbReader;
import cn.abelib.jodis.log.JdbWriter;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.ErrorResponse;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;
import cn.abelib.jodis.impl.executor.ExecutorFactory;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.server.JodisException;
import cn.abelib.jodis.utils.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 */
public class JodisDb {
    /**
     * 存储不带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> jodisCollection;

    /**
     * 存储带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    private ExecutorFactory executorFactory;
    /**
     * Aof文件写入
     */
    private AofWriter aofWriter;
    /**
     * 请求队列
     */
    private List<Request> requestQueue;

    /**
     * todo
     */
    private JdbReader jdbReader;

    private JdbWriter jdbWriter;

    /**
     * 是否正在进行Aof文件重写
     */
    private AtomicBoolean rewriteAof;

    public JodisDb(JodisConfig jodisConfig) throws IOException {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
        aofWriter = new AofWriter(jodisConfig.getLogDir(), jodisConfig.getLogWal());

        requestQueue = new ArrayList<>(10);
        rewriteAof = new AtomicBoolean(false);
    }

    /**
     * default for test
     * todo
     * @throws IOException
     */
    public JodisDb() throws IOException {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
        requestQueue = new ArrayList<>(10);
        rewriteAof = new AtomicBoolean(false);
    }

    public ConcurrentHashMap<String, JodisObject> jodisCollection() {
        return this.jodisCollection;
    }

    public ConcurrentHashMap<String, JodisObject> expireJodisCollection() {
        return this.expireJodisCollection;
    }

    public JodisObject put(String key, JodisObject value) {
        return jodisCollection.put(key, value);
    }

    /**
     * 执行命令
     * @param request
     * @return
     */
    public Response execute(Request request) throws IOException {
        String cmd = request.getCmd();
        List<String> params = request.getArgs();
        Response result;
        if (ProtocolConstant.KEY_CMDS.contains(cmd)) {
            // todo
            result = executorFactory.stringExecutor().execute(cmd, params);
        } else if (ProtocolConstant.STRING_CMDS.contains(cmd)) {
            result = executorFactory.stringExecutor().execute(cmd, params);
        } else if (ProtocolConstant.LIST_CMDS.contains(cmd)) {
            result = executorFactory.listExecutor().execute(cmd, params);
        } else if (ProtocolConstant.HASH_CMDS.contains(cmd)) {
            result = executorFactory.hashExecutor().execute(cmd, params);
        } else if (ProtocolConstant.SET_CMDS.contains(cmd)) {
            result =  executorFactory.setExecutor().execute(cmd, params);
        } else if (ProtocolConstant.ZSET_CMDS.contains(cmd)) {
            result = executorFactory.zSetExecutor().execute(cmd, params);
        } else {
            // todo not support now
            result = ErrorResponse.errorSyntax();
        }

        // 检测是否需要进行AOF
        if (request.needLog() || !result.isError()) {
            // 如果正在
            if (rewriteAof.get()) {
                requestQueue.add(request);
            } else {
                aofWriter.write(request.getRequest());
            }

        }

        // todo
        return result;
    }


    public Response execute(String request) throws IOException {
        Request reqCmd = new Request(request);
        return execute(reqCmd);
    }


    public Map<String, JodisObject> copy() {
        return this.jodisCollection
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    public void rewriteAof() throws IOException {
        Map<String, JodisObject> source = copy();
        this.requestQueue.clear();
        this.rewriteAof.set(true);
        this.aofWriter.startRewrite();
        for (Map.Entry<String, JodisObject> entry : source.entrySet()) {
            JodisObject value =  entry.getValue();
            String type = value.type();
            String key = entry.getKey();

            Request cmd;
            switch (type) {
                case KeyType.JODIS_STRING:
                    String valueString = ((JodisString)value.getValue()).getHolder();
                    cmd = Request.stringSetCmd(key, valueString);
                    break;
                case KeyType.JODIS_HASH:
                    Map<String, String> valueMap = ((JodisHash)value.getValue()).getHolder();
                    cmd = Request.hashMultiSetCmd(key, valueMap);
                    break;
                case KeyType.JODIS_SET:
                    Set<String> valueSet = ((JodisSet)value.getValue()).getHolder();
                    cmd = Request.setAddCmd(key, valueSet);
                    break;
                case KeyType.JODIS_LIST:
                    List<String> valueList = ((JodisList)value.getValue()).getHolder();
                    cmd = Request.listPushCmd(key, valueList);
                    break;
                case KeyType.JODIS_ZSET:
                    Map<String, Double> valueZset = ((JodisSortedSet)value.getValue()).getHolder();
                    cmd = Request.zsetAddCmd(key, valueZset);
                    break;
                default:
                    cmd = null;
            }
            if (Objects.nonNull(cmd) && StringUtils.isNotEmpty(cmd.toString())) {
                this.aofWriter.rewrite(cmd.toString());
            }
        }
        Iterator<Request> iterator = this.requestQueue.iterator();
        while (iterator.hasNext()) {
            this.aofWriter.rewrite(iterator.next().toString());
        }
        this.rewriteAof.set(false);
    }


}
