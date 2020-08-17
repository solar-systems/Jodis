package cn.abelib.jodis.impl;

import cn.abelib.jodis.log.WalReader;
import cn.abelib.jodis.log.WalWriter;
import cn.abelib.jodis.log.JdbReader;
import cn.abelib.jodis.log.JdbWriter;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.impl.executor.ExecutorFactory;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.CollectionUtils;
import cn.abelib.jodis.utils.Logger;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 */
public class JodisDb {
    private Logger logger = Logger.getLogger(JodisDb.class);

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

    private JodisConfig jodisConfig;

    /**
     * Wal文件写入
     */
    private WalWriter walWriter;
    /**
     * WalReader
     */
    private WalReader walReader;

    /**
     * 请求队列
     */
    private List<Request> requestQueue;

    /**
     * jdbReader
     */
    private JdbReader jdbReader;

    private JdbWriter jdbWriter;

    private boolean noNeed;

    /**
     * 是否正在进行Wal文件重写
     */
    private AtomicBoolean rewriteWal;

    public JodisDb(JodisConfig jodisConfig) throws IOException {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
        walWriter = new WalWriter(jodisConfig.getLogDir(), jodisConfig.getLogWal());
        walReader = new WalReader(jodisConfig.getLogDir(), jodisConfig.getLogWal());
        requestQueue = new ArrayList<>(10);
        rewriteWal = new AtomicBoolean(false);

        respParser = new RespParser();
        noNeed = false;
        this.jodisConfig = jodisConfig;
        loadFromLog();
    }

    /**
     * default for test
     */
    public JodisDb() {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
        requestQueue = new ArrayList<>(10);
        rewriteWal = new AtomicBoolean(false);
        noNeed = true;
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
     * clear
     */
    public void clear() {
        this.jodisCollection.clear();
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
        // 检测是否需要进行Wal
        if (!noNeed && request.needLog() && !response.isError()) {
            // 如果正在进行Wal重写
            if (rewriteWal.get()) {
                requestQueue.add(request);
            } else {
                walWriter.write(request.getRequest());
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

    public Response execute(String request, boolean isNeed) throws IOException {
        Request req = respParser.parse(request);
        req.needLog(isNeed);
        return execute(req);
    }

    /**
     * todo
     * 从磁盘加载数据
     */
    public void loadFromLog() throws IOException {
        int loadMode = jodisConfig.getReloadMode();
        if (loadMode == JodisConstant.WAL_MODE) {
            logger.info("Load from disk with WAL mode started");
            Stopwatch stopwatch = Stopwatch.createStarted();
            loadFromWal();
            logger.info("Load from disk with WAL mode finished, cost: {}", stopwatch.stop().toString());
        } else if (loadMode == JodisConstant.JDB_MODE) {
            logger.info("Load from disk with JDB mode started");
            Stopwatch stopwatch = Stopwatch.createStarted();

            logger.info("Load from disk with JDB mode finished, cost: {}", stopwatch.stop().toString());
        } else if (loadMode == JodisConstant.MIX_MODE) {
            logger.info("Load from disk with MIX mode started");
            Stopwatch stopwatch = Stopwatch.createStarted();
            loadFromWal();
            logger.info("Load from disk with MIX mode finished, cost: {}", stopwatch.stop().toString());
        } else {
            logger.warn("Invalid load mode :{}", loadMode);
        }
    }

    private void loadFromWal() throws IOException {
        Iterator<String> iterator = walReader.readWal();
        while (iterator.hasNext()) {
            String request = iterator.next();
            if (StringUtils.isNotEmpty(request)) {
                execute(request.trim(), false);
            }
        }
    }

    /**
     * todo
     */
    private void loadFromJdb() {

    }

    /**
     * todo 合理位置调用 rewriteWal
     * 重写的规模，比如multi操作的最大值
     * Wal重写
     * @throws IOException
     */
    public void rewriteWal() throws IOException {
        Map<String, JodisObject> source = CollectionUtils.deepCopyMap(this.jodisCollection);
        this.requestQueue.clear();
        this.rewriteWal.set(true);
        this.walWriter.startRewrite();
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
            if (Objects.nonNull(cmd) && StringUtils.isNotEmpty(cmd.toString()) && !cmd.isError()) {
                this.walWriter.rewrite(cmd.toString());
            }
        }
        Iterator<Request> iterator = this.requestQueue.iterator();
        while (iterator.hasNext()) {
            this.walWriter.rewrite(iterator.next().toString());
        }
        this.rewriteWal.set(false);
    }
}
