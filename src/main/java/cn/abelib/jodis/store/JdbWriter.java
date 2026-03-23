package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * JDB 文件写入器，使用 mmap 实现高性能写入
 *
 * 文件结构：
 * |-----|--------|------|------|
 * |type |totalLen|  key | value|
 * |-----|--------|------|------|
 *
 * JDB 文件适合使用 mmap，因为：
 * 1. 批量写入全量数据
 * 2. 文件大小可以预先计算
 * 3. 写入完成后一次性刷盘
 *
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 */
public class JdbWriter {
    private static final Logger logger = Logger.getLogger(JdbWriter.class);

    private final String filePath;
    private final int fileSize;
    private MappedFile mappedFile;
    private boolean closed = false;

    /**
     * 使用 StoreConfig 创建 JDB 写入器
     *
     * @param config 存储配置
     * @throws IOException IO异常
     */
    public JdbWriter(StoreConfig config) throws IOException {
        this(config.getLogDir(), config.getJdbFile(), config.getJdbFileSize());
    }

    /**
     * 创建 JDB 写入器（使用默认文件大小）
     *
     * @param dir    目录
     * @param fName  文件名
     * @throws IOException IO异常
     */
    public JdbWriter(String dir, String fName) throws IOException {
        this(dir, fName, StoreConfig.DEFAULT_JDB_FILE_SIZE);
    }

    /**
     * 创建 JDB 写入器（指定文件大小）
     *
     * @param dir      目录
     * @param fName    文件名
     * @param fileSize 文件大小
     * @throws IOException IO异常
     */
    public JdbWriter(String dir, String fName, int fileSize) throws IOException {
        this.filePath = dir + (dir.endsWith("/") ? "" : "/") + fName;
        this.fileSize = fileSize;

        // 删除已存在的文件，确保每次都是从头开始写入
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        this.mappedFile = new MappedFile(filePath, fileSize);

        // 写入文件头
        writeFileHeader();
    }

    /**
     * 写入文件头（Magic Flag + Version）
     */
    private void writeFileHeader() throws IOException {
        byte[] magic = JdbConstant.JDB_MAGIC_FLAG.getBytes();
        byte[] version = JdbConstant.JDB_VERSION.getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(magic.length + 1 + version.length + 1);
        buffer.put((byte) magic.length);
        buffer.put(magic);
        buffer.put((byte) version.length);
        buffer.put(version);
        buffer.flip();

        mappedFile.appendMessage(buffer.array());
    }

    /**
     * 写入快照数据
     *
     * @param data 内存数据库中的所有数据
     * @throws IOException IO异常
     */
    public void writeSnapshot(Map<String, JodisObject> data) throws IOException {
        if (closed) {
            logger.warn("Cannot write snapshot, JdbWriter is already closed");
            return;
        }

        if (data == null || data.isEmpty()) {
            logger.info("No data to write, skip snapshot");
            return;
        }

        try {
            int count = 0;

            for (Map.Entry<String, JodisObject> entry : data.entrySet()) {
                JodisObject obj = entry.getValue();
                String type = obj.type();

                ByteBuffer buffer;
                switch (type) {
                    case KeyType.JODIS_STRING:
                        buffer = writeString(entry.getKey(), obj);
                        break;
                    case KeyType.JODIS_LIST:
                        buffer = writeList(entry.getKey(), obj);
                        break;
                    case KeyType.JODIS_HASH:
                        buffer = writeHash(entry.getKey(), obj);
                        break;
                    case KeyType.JODIS_SET:
                        buffer = writeSet(entry.getKey(), obj);
                        break;
                    case KeyType.JODIS_ZSET:
                        buffer = writeZSet(entry.getKey(), obj);
                        break;
                    default:
                        logger.warn("Unknown type: {} for key: {}", type, entry.getKey());
                        continue;
                }

                if (buffer != null) {
                    mappedFile.appendMessage(buffer.array());
                    count++;
                }
            }

            // 写入 EOF 标记
            writeFileFooter();

            // 强制刷盘
            mappedFile.force();

            // 截断文件到实际大小
            mappedFile.truncate();

            logger.info("Write JDB snapshot completed, file: {}, total keys: {}", filePath, count);
        } catch (Exception e) {
            logger.error("Write JDB snapshot failed", e);
            throw new IOException("Failed to write JDB snapshot", e);
        }
    }

    /**
     * 写入文件尾（EOF 标记）
     */
    private void writeFileFooter() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(JdbConstant.EOF);
        buffer.flip();
        mappedFile.appendMessage(buffer.array());
    }

    /**
     * For JodisString
     * |-----|--------|-----|------|------|----------|
     * |type |totalLen|kLen | key  |value |expireTime|
     * |-----|--------|-----|------|------|----------|
     */
    public ByteBuffer writeString(String key, JodisObject jodisObject) {
        JodisString value = (JodisString) jodisObject.getValue();
        long expireTime = jodisObject.getExpireTime();

        String valueString = value.getHolder();
        byte[] keys = ByteUtils.stringBytesWithLen(key);
        byte[] values = ByteUtils.stringBytesWithLen(valueString);
        int keysLen = keys.length;
        int valuesLen = values.length;

        int len = JdbConstant.JDB_TYPE_SIZE + JdbConstant.JDB_INT_SIZE + keysLen + valuesLen + JdbConstant.JDB_LONG_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(len);

        buffer.put(JdbConstant.JDB_TYPE_STRING);
        buffer.putInt(len);
        buffer.put(keys);
        buffer.put(values);
        buffer.putLong(expireTime);

        buffer.flip();
        return buffer;
    }

    /**
     * For JodisList
     * |-----|--------|-----|-------|-------|------|-------|----------|
     * |type |totalLen|kLen | key   |listLen|item  |  ...  |expireTime|
     * |-----|--------|-----|-------|-------|------|-------|----------|
     */
    public ByteBuffer writeList(String key, JodisObject jodisObject) {
        JodisList value = (JodisList) jodisObject.getValue();
        List<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_LIST, jodisObject.getExpireTime());
    }

    /**
     * For JodisHash
     * |-----|--------|-----|-------|--------|-------|--------|-------|------|----------|
     * |type |totalLen|kLen | key   |hashLen |field  | value  |  ...   |  ... |expireTime|
     * |-----|--------|-----|-------|--------|-------|--------|-------|------|----------|
     */
    public ByteBuffer writeHash(String key, JodisObject jodisObject) {
        JodisHash value = (JodisHash) jodisObject.getValue();
        Map<String, String> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.size() * 2);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            kvs.add(entry.getKey());
            kvs.add(entry.getValue());
        }
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_HASH, jodisObject.getExpireTime());
    }

    /**
     * For JodisSet
     * |-----|--------|-----|-------|-------|------|-------|----------|
     * |type |totalLen|kLen | key   |setLen |item  |  ...  |expireTime|
     * |-----|--------|-----|-------|-------|------|-------|----------|
     */
    public ByteBuffer writeSet(String key, JodisObject jodisObject) {
        JodisSet value = (JodisSet) jodisObject.getValue();
        Set<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_SET, jodisObject.getExpireTime());
    }

    /**
     * For JodisSortedSet
     * |-----|--------|-----|-------|--------|-------|--------|-------|------|----------|
     * |type |totalLen|kLen | key   |zsetLen |member | score  |  ...   |  ... |expireTime|
     * |-----|--------|-----|-------|--------|-------|--------|-------|------|----------|
     */
    public ByteBuffer writeZSet(String key, JodisObject jodisObject) {
        JodisSortedSet value = (JodisSortedSet) jodisObject.getValue();
        Map<String, Double> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.size() * 2);
        values.forEach((k, v) -> {
            kvs.add(k);
            kvs.add(String.valueOf(v));
        });
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_ZSET, jodisObject.getExpireTime());
    }

    private ByteBuffer writeCollection(String key, Collection<String> value, byte jdbType, long expireTime) {
        int totalLen = 0;
        List<byte[]> valuesBytes = new ArrayList<>(value.size());
        for (String item : value) {
            byte[] bytes = ByteUtils.stringBytesWithLen(item);
            totalLen += bytes.length;
            valuesBytes.add(bytes);
        }
        byte[] keys = ByteUtils.stringBytesWithLen(key);
        int keysLen = keys.length;
        totalLen += keysLen;
        totalLen += JdbConstant.JDB_INT_SIZE;
        totalLen += JdbConstant.JDB_TYPE_SIZE + JdbConstant.JDB_INT_SIZE + JdbConstant.JDB_LONG_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        buffer.put(jdbType);
        buffer.putInt(totalLen);
        buffer.put(keys);
        buffer.putInt(value.size());
        for (byte[] item : valuesBytes) {
            buffer.put(item);
        }
        buffer.putLong(expireTime);

        buffer.flip();
        return buffer;
    }

    /**
     * 获取文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 获取写入位置
     */
    public int getWrotePosition() {
        return mappedFile.getWrotePosition().get();
    }

    /**
     * 关闭资源
     */
    public void close() throws IOException {
        if (!closed) {
            if (mappedFile != null) {
                mappedFile.close();
            }
            closed = true;
            logger.info("JdbWriter closed: {}", filePath);
        }
    }
}
