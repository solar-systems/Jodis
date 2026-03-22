package cn.abelib.jodis.impl;

import cn.abelib.jodis.store.WalReader;
import cn.abelib.jodis.store.WalWriter;
import cn.abelib.jodis.store.JdbReader;
import cn.abelib.jodis.store.JdbWriter;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.impl.executor.ExecutorFactory;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.CollectionUtils;
import cn.abelib.jodis.utils.Logger;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.base.Stopwatch;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 */
public class JodisDb implements Closeable {
    private Logger logger = Logger.getLogger(JodisDb.class);

    /**
     * 存储所有 key（包括带 TTL 的和不带 TTL的）
     * TTL 信息保存在 JodisObject 的 ttl 和 created 字段中
     */
    private ConcurrentHashMap<String, JodisObject> jodisCollection;

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
        // 初始化 JDB 写入器和读取器
        jdbWriter = new JdbWriter(jodisConfig.getLogDir(), jodisConfig.getLogJdb());
        jdbReader = new JdbReader(jodisConfig.getLogDir(), jodisConfig.getLogJdb());
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
            loadFromJdb();
            logger.info("Load from disk with JDB mode finished, cost: {}", stopwatch.stop().toString());
        } else if (loadMode == JodisConstant.MIX_MODE) {
            logger.info("Load from disk with MIX mode started");
            Stopwatch stopwatch = Stopwatch.createStarted();
            loadFromJdb();
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
     * 从 JDB 快照加载数据
     */
    private void loadFromJdb() throws IOException {
        try {
            Map<String, JodisObject> data = jdbReader.readSnapshot();
            jodisCollection.putAll(data);
        } catch (IOException e) {
            logger.warn("Load from JDB failed: {}. Will continue with empty database.", e.getMessage());
        }
    }

    /**
     * 保存快照到 JDB 文件
     * @throws IOException
     */
    public void saveSnapshot() throws IOException {
        logger.info("Save snapshot to JDB started");
        Stopwatch stopwatch = Stopwatch.createStarted();
            
        // 复制当前数据，避免并发修改
        Map<String, JodisObject> snapshot = CollectionUtils.deepCopyMap(this.jodisCollection);
            
        // 写入 JDB 文件
        jdbWriter.writeSnapshot(snapshot);
            
        logger.info("Save snapshot to JDB completed, total keys: {}, cost: {}", 
                   snapshot.size(), stopwatch.stop().toString());
    }
        
    /**
     * todo 合理位置调用 rewriteWal
     * 重写的规模，比如 multi 操作的最大值
     * Wal 重写
     * @throws IOException
     */
    public void rewriteWal() throws IOException {
        Map<String, JodisObject> source = CollectionUtils.deepCopyMap(this.jodisCollection);
        this.rewriteWal.set(true);
        this.walWriter.startRewrite();
        
        // 先将内存中的数据快照写入新 WAL
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
        
        // 再将重写期间累积的新请求追加到新 WAL
        Iterator<Request> iterator = this.requestQueue.iterator();
        while (iterator.hasNext()) {
            this.walWriter.rewrite(iterator.next().toString());
        }
        
        this.rewriteWal.set(false);
        this.requestQueue.clear();  // 清空队列，因为已经写入新 WAL
            
        // 原子替换 WAL 文件
        completeWalRewrite();
    }
        
    /**
     * 完成 WAL 文件替换
     */
    private void completeWalRewrite() throws IOException {
        Path walPath = Paths.get(jodisConfig.getLogDir(), jodisConfig.getLogWal());
        Path rewritePath = Paths.get(jodisConfig.getLogDir(), jodisConfig.getLogWal() + ".rewrite");
            
        if (Files.exists(rewritePath)) {
            // 备份旧 WAL
            Path backupPath = walPath.resolveSibling(walPath.getFileName() + ".bak");
            Files.move(walPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
            // 替换为新文件
            Files.move(rewritePath, walPath, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                
            // 删除备份
            Files.deleteIfExists(backupPath);
                
            logger.info("WAL rewrite completed, new file size: {} bytes", Files.size(walPath));
        } else {
            logger.warn("WAL rewrite file does not exist, skipping replacement");
        }
    }
    
    @Override
    public void close() throws IOException {
        logger.info("Closing JodisDb...");
        
        // 只在有数据且未关闭时才保存快照
        if (!jodisCollection.isEmpty()) {
            try {
                saveSnapshot();
            } catch (Exception e) {
                logger.error("Failed to save snapshot on close", e);
            }
        }
        
        // 关闭 WAL 写入器（只关闭一次）
        if (walWriter != null) {
            walWriter.close();
            walWriter = null;  // 防止重复关闭
        }
        
        // 关闭 JDB 写入器（只关闭一次）
        if (jdbWriter != null) {
            jdbWriter.close();
            jdbWriter = null;  // 防止重复关闭
        }
        
        logger.info("JodisDb closed");
    }
}
