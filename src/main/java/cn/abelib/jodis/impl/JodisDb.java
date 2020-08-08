package cn.abelib.jodis.impl;

import cn.abelib.jodis.log.AofWriter;
import cn.abelib.jodis.log.JdbReader;
import cn.abelib.jodis.log.JdbWriter;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.impl.executor.ExecutorFactory;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.CollectionUtils;
import cn.abelib.jodis.utils.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
     *  todo TTL
     * 存储带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    private ExecutorFactory executorFactory;

    private RespParser respParser;

    /**
     * Aof文件写入
     */
    private AofWriter aofWriter;
    /**
     * 请求队列
     */
    private List<Request> requestQueue;

    /**
     * todo jdb
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

        respParser = new RespParser();
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

    public JodisObject put(String key, JodisObject value) {
        return jodisCollection.put(key, value);
    }

    public JodisObject get(String key) {
        return jodisCollection.get(key);
    }

    /**
     * @return
     */
    public int size() {
        return jodisCollection.size();
    }

    /**
     * @param key
     */
    public void remove(String key) {
        this.jodisCollection.remove(key);
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return this.jodisCollection.containsKey(key);
    }

    /**
     * @return
     */
    public Set<String> keySet() {
        return this.jodisCollection.keySet();
    }

    /**
     * 执行命令
     * @param request
     * @return
     */
    public Response execute(Request request) throws IOException {
        Response response = executorFactory.execute(request);
        // 检测是否需要进行AOF
        if (request.needLog() || !response.isError()) {
            // 如果正在进行Aof重写
            if (rewriteAof.get()) {
                requestQueue.add(request);
            } else {
                aofWriter.write(request.getRequest());
            }

        }
        return response;
    }

    /**
     * 执行
     * @param request
     * @return
     * @throws IOException
     */
    public Response execute(String request) throws IOException {
        Request req = respParser.parse(request);
        return execute(req);
    }

    /**
     * todo 合理位置调用 rewriteaof
     * Aof重写
     * @throws IOException
     */
    public void rewriteAof() throws IOException {
        Map<String, JodisObject> source = CollectionUtils.deepCopyMap(this.jodisCollection);
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
                    cmd = respParser.stringSetCmd(key, valueString);
                    break;
                case KeyType.JODIS_HASH:
                    Map<String, String> valueMap = ((JodisHash)value.getValue()).getHolder();
                    cmd = respParser.hashMultiSetCmd(key, valueMap);
                    break;
                case KeyType.JODIS_SET:
                    Set<String> valueSet = ((JodisSet)value.getValue()).getHolder();
                    cmd = respParser.setAddCmd(key, valueSet);
                    break;
                case KeyType.JODIS_LIST:
                    List<String> valueList = ((JodisList)value.getValue()).getHolder();
                    cmd = respParser.listPushCmd(key, valueList);
                    break;
                case KeyType.JODIS_ZSET:
                    Map<String, Double> valueZset = ((JodisSortedSet)value.getValue()).getHolder();
                    cmd = respParser.sortedSetAddCmd(key, valueZset);
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
