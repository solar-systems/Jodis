package cn.abelib.jodis.impl;

import cn.abelib.jodis.persist.AofWriter;
import cn.abelib.jodis.protocol.CmdConstant;
import cn.abelib.jodis.protocol.ErrorCmd;
import cn.abelib.jodis.protocol.ReqCmd;
import cn.abelib.jodis.protocol.RespCmd;
import cn.abelib.jodis.protocol.executor.ExecutorFactory;
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
    private List<ReqCmd> requestQueue;
    /**
     * 是否正在进行Aof文件重写
     */
    private AtomicBoolean rewriteAof;

    public JodisDb() throws IOException {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
        // todo 配置
        aofWriter = new AofWriter("", "");
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
    public RespCmd execute(ReqCmd request) throws IOException {
        String cmd = request.getCmd();
        List<String> params = request.getParams();
        RespCmd result;
        if (CmdConstant.KEY_CMDS.contains(cmd)) {
            // todo
            result = executorFactory.stringExecutor().execute(cmd, params);
        } else if (CmdConstant.STRING_CMDS.contains(cmd)) {
            result = executorFactory.stringExecutor().execute(cmd, params);
        } else if (CmdConstant.LIST_CMDS.contains(cmd)) {
            result = executorFactory.listExecutor().execute(cmd, params);
        } else if (CmdConstant.HASH_CMDS.contains(cmd)) {
            result = executorFactory.hashExecutor().execute(cmd, params);
        } else if (CmdConstant.SET_CMDS.contains(cmd)) {
            result =  executorFactory.setExecutor().execute(cmd, params);
        } else if (CmdConstant.ZSET_CMDS.contains(cmd)) {
            result = executorFactory.zSetExecutor().execute(cmd, params);
        } else {
            // todo not support now
            result = ErrorCmd.errorSyntax();
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


    public RespCmd execute(String request) throws IOException {
        ReqCmd reqCmd = new ReqCmd(request);
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

            ReqCmd cmd;
            switch (type) {
                case ObjectType.JODIS_STRING:
                    String valueString = ((JodisString)value.getValue()).getHolder();
                    cmd = ReqCmd.stringSetCmd(key, valueString);
                    break;
                case ObjectType.JODIS_HASH:
                    Map<String, String> valueMap = ((JodisMap)value.getValue()).getHolder();
                    cmd = ReqCmd.hashMultiSetCmd(key, valueMap);
                    break;
                case ObjectType.JODIS_SET:
                    Set<String> valueSet = ((JodisSet)value.getValue()).getHolder();
                    cmd = ReqCmd.setAddCmd(key, valueSet);
                    break;
                case ObjectType.JODIS_LIST:
                    List<String> valueList = ((JodisList)value.getValue()).getHolder();
                    cmd = ReqCmd.listPushCmd(key, valueList);
                    break;
                case ObjectType.JODIS_ZSET:
                    Map<String, Double> valueZset = ((JodisZSet)value.getValue()).getHolder();
                    cmd = ReqCmd.zsetAddCmd(key, valueZset);
                    break;
                default:
                    cmd = null;
            }
            if (Objects.nonNull(cmd) && StringUtils.isNotEmpty(cmd.toString())) {
                this.aofWriter.rewrite(cmd.toString());
            }
        }
        Iterator<ReqCmd> iterator = this.requestQueue.iterator();
        while (iterator.hasNext()) {
            this.aofWriter.rewrite(iterator.next().toString());
        }
        this.rewriteAof.set(false);
    }


}
