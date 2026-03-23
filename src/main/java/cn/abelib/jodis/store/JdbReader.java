package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.impl.SkipList;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.KeyValue;
import cn.abelib.jodis.utils.Logger;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.*;


/**
 * JDB 文件读取器，使用 mmap 实现高性能读取
 *
 * JDB 文件适合使用 mmap，因为：
 * 1. 顺序读取整个文件
 * 2. 读取时一次性加载到内存
 * 3. 随机访问性能好
 *
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 * Jdb means Jodis Database
 */
public class JdbReader {
    private static final Logger logger = Logger.getLogger(JdbReader.class);

    private Path jdbFilePath;
    private MappedFile mappedFile;
    private MappedByteBuffer mappedByteBuffer;
    private volatile boolean closed = false;

    /**
     * 使用 StoreConfig 创建 JDB 读取器
     *
     * @param config 存储配置
     * @throws IOException IO异常
     */
    public JdbReader(StoreConfig config) throws IOException {
        this(config.getLogDir(), config.getJdbFile());
    }

    /**
     * 默认构造函数（不初始化文件，用于单元测试）
     */
    public JdbReader() {
    }

    /**
     * 创建 JDB 读取器
     *
     * @param dir   目录
     * @param fName 文件名
     * @throws IOException IO异常
     */
    public JdbReader(String dir, String fName) throws IOException {
        this.jdbFilePath = IoUtils.createFileIfNotExists(dir, fName);

        // 如果文件存在且有内容，创建 MappedFile
        File file = this.jdbFilePath.toFile();
        if (file.exists() && file.length() > 0) {
            this.mappedFile = new MappedFile(file.getAbsolutePath(), (int) file.length());
            this.mappedByteBuffer = mappedFile.getMappedByteBuffer();
        }
    }

    /**
     * 读取快照文件，返回所有数据
     *
     * @return Map<String, JodisObject>
     * @throws IOException IO异常
     */
    public Map<String, JodisObject> readSnapshot() throws IOException {
        Map<String, JodisObject> data = new HashMap<>();
        long now = System.currentTimeMillis();

        // 如果文件不存在或为空，直接返回空数据
        if (jdbFilePath == null || !jdbFilePath.toFile().exists() || jdbFilePath.toFile().length() == 0) {
            logger.info("JDB file does not exist or is empty, returning empty data");
            return data;
        }

        if (mappedByteBuffer == null) {
            logger.warn("MappedByteBuffer is null, returning empty data");
            return data;
        }

        try {
            // 1. 验证文件头
            int magicLen = mappedByteBuffer.get();
            byte[] magicBytes = new byte[magicLen];
            mappedByteBuffer.get(magicBytes);
            String magic = new String(magicBytes);

            if (!JdbConstant.JDB_MAGIC_FLAG.equals(magic)) {
                throw new IOException("Invalid JDB file: bad magic flag '" + magic + "', expected '" + JdbConstant.JDB_MAGIC_FLAG + "'");
            }

            int versionLen = mappedByteBuffer.get();
            byte[] versionBytes = new byte[versionLen];
            mappedByteBuffer.get(versionBytes);
            String version = new String(versionBytes);

            if (!"0001".equals(version)) {
                logger.warn("Unsupported JDB version: {}, but will try to read", version);
            }

            logger.info("Read JDB file started, version: {}", version);

            // 2. 读取所有 Entry
            int count = 0;
            int expiredCount = 0;

            while (mappedByteBuffer.hasRemaining()) {
                byte type = mappedByteBuffer.get();
                if (type == JdbConstant.EOF) {
                    break;  // 文件结束
                }

                int totalLen = mappedByteBuffer.getInt();
                ByteBuffer entryBuffer = ByteBuffer.allocate(totalLen - 1); // 减去 type 占用的 1 字节
                for (int i = 0; i < totalLen - 1; i++) {
                    entryBuffer.put(mappedByteBuffer.get());
                }

                entryBuffer.flip();
                KeyValue<String, JodisObject> entry = parseEntry(type, entryBuffer);

                if (entry != null && !entry.isNull()) {
                    // 检查是否过期
                    if (entry.getValue().getExpireTime() > 0 && entry.getValue().getExpireTime() < now) {
                        expiredCount++;
                        logger.debug("Skip expired key: {}", entry.getKey());
                    } else {
                        data.put(entry.getKey(), entry.getValue());
                        count++;
                    }
                }
            }

            logger.info("Read JDB file completed, total keys: {}, expired keys: {}", count, expiredCount);
            return data;
        } catch (Exception e) {
            logger.error("Failed to read JDB file: {}", e.getMessage(), e);
            throw new IOException("Failed to read JDB snapshot", e);
        }
    }

    /**
     * 解析 Entry 数据
     */
    private KeyValue<String, JodisObject> parseEntry(byte type, ByteBuffer buffer) {
        try {
            switch (type) {
                case JdbConstant.JDB_TYPE_STRING:
                    return readString(buffer);
                case JdbConstant.JDB_TYPE_LIST:
                    return readList(buffer);
                case JdbConstant.JDB_TYPE_HASH:
                    return readMap(buffer);
                case JdbConstant.JDB_TYPE_SET:
                    return readSet(buffer);
                case JdbConstant.JDB_TYPE_ZSET:
                    return readZSet(buffer);
                default:
                    logger.warn("Unknown entry type: {}", type);
                    return null;
            }
        } catch (Exception e) {
            logger.error("Failed to parse entry with type: {}", type, e);
            return null;
        }
    }

    public KeyValue<String, JodisObject> readString(ByteBuffer buffer) {
        if (buffer == null || buffer.remaining() < 1) {
            return new KeyValue<>();
        }

        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        int valueLen = buffer.getInt();
        String value = ByteUtils.toUTF8String(buffer, valueLen);
        // 读取 TTL 信息
        long expireTime = buffer.getLong();

        JodisString jodisString = new JodisString(value);
        JodisObject obj = new JodisObject(jodisString, KeyType.JODIS_STRING, EncodingType.OBJ_ENCODING_RAW.getType());
        obj.setExpireTime(expireTime);

        return new KeyValue<>(key, obj);
    }

    public KeyValue<String, JodisObject> readList(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        long expireTime = ans.getExpireTime();

        JodisList jodisList = new JodisList(value);
        JodisObject obj = new JodisObject(jodisList, KeyType.JODIS_LIST, EncodingType.OBJ_ENCODING_LIST.getType());
        obj.setExpireTime(expireTime);

        return new KeyValue<>(key, obj);
    }

    public KeyValue<String, JodisObject> readMap(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        long expireTime = ans.getExpireTime();

        int len = value.size() / 2;
        Map<String, String> map = new HashMap<>(len);
        for (int i = 0; i < len; i++) {
            map.put(value.get(i * 2), value.get(i * 2 + 1));
        }
        JodisHash jodisHash = new JodisHash(map);
        JodisObject obj = new JodisObject(jodisHash, KeyType.JODIS_HASH, EncodingType.OBJ_ENCODING_HT.getType());
        obj.setExpireTime(expireTime);

        return new KeyValue<>(key, obj);
    }

    public KeyValue<String, JodisObject> readSet(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        long expireTime = ans.getExpireTime();

        JodisSet jodisSet = new JodisSet(Sets.newHashSet(value));
        JodisObject obj = new JodisObject(jodisSet, KeyType.JODIS_SET, EncodingType.OBJ_ENCODING_SET.getType());
        obj.setExpireTime(expireTime);

        return new KeyValue<>(key, obj);
    }

    public KeyValue<String, JodisObject> readZSet(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        long expireTime = ans.getExpireTime();

        int len = value.size() / 2;
        Map<String, Double> map = new HashMap<>(len);
        SkipList skipList = new SkipList();
        for (int i = 0; i < len; i++) {
            String item = value.get(i * 2);
            Double score = Double.valueOf(value.get(i * 2 + 1));
            map.put(item, score);
            skipList.add(score, item);
        }
        JodisSortedSet jodisSortedSet = new JodisSortedSet(map, skipList);
        JodisObject obj = new JodisObject(jodisSortedSet, KeyType.JODIS_ZSET, EncodingType.OBJ_ENCODING_SKIPLIST.getType());
        obj.setExpireTime(expireTime);

        return new KeyValue<>(key, obj);
    }

    private KeyValue<String, List<String>> readCollection(ByteBuffer buffer) {
        if (buffer == null || buffer.remaining() < 1) {
            return new KeyValue<>();
        }

        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        // 集合长度
        int len = buffer.getInt();
        List<String> value = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            int itemLen = buffer.getInt();
            String item = ByteUtils.toUTF8String(buffer, itemLen);
            value.add(item);
        }
        // 读取 TTL 信息
        long expireTime = buffer.getLong();

        KeyValue<String, List<String>> result = new KeyValue<>(key, value);
        result.setExpireTime(expireTime);
        return result;
    }

    /**
     * 获取文件路径
     */
    public Path getJdbFilePath() {
        return jdbFilePath;
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (!closed) {
            if (mappedFile != null) {
                mappedFile.close();
            }
            closed = true;
            logger.info("JdbReader closed");
        }
    }
}
