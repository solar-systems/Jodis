package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 *
 *  总体结构
 * |-----|--------|------|------|
 * |type |totalLen|  key | value|
 * |-----|--------|------|------|
 */
public class JdbWriter {
    private Logger logger = Logger.getLogger(JdbWriter.class);
    private Path jdbFile;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private boolean closed = false;  // 标记是否已关闭
    
    public JdbWriter(String dir, String fName) throws IOException {
        this.jdbFile = Paths.get(dir, fName);
        // 确保目录存在
        if (!Files.exists(this.jdbFile.getParent())) {
            Files.createDirectories(this.jdbFile.getParent());
        }
        this.fos = new FileOutputStream(this.jdbFile.toFile());
        this.bos = new BufferedOutputStream(fos);
        
        // 写入文件头
        writeFileHeader();
    }

    /**
     * 写入文件头（Magic Flag + Version）
     */
    private void writeFileHeader() throws IOException {
        byte[] magic = JdbConstant.JDB_MAGIC_FLAG.getBytes();
        byte[] version = JdbConstant.JDB_VERSION.getBytes();
        
        bos.write(magic.length);
        bos.write(magic);
        bos.write(version.length);
        bos.write(version);
        bos.flush();
    }
    
    /**
     * 写入文件尾（EOF 标记）
     */
    private void writeFileFooter() throws IOException {
        bos.write(JdbConstant.EOF);
        bos.flush();
        // 强制刷盘
        fos.getChannel().force(true);
    }
    
    /**
     * 写入快照数据
     */
    public void writeSnapshot(Map<String, JodisObject> data) throws IOException {
        if (closed) {
            logger.warn("Cannot write snapshot, JdbWriter is already closed");
            return;
        }
        
        try {
            // 遍历所有 Key
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
                
                // 写入 ByteBuffer 到文件
                bos.write(buffer.array());
            }
            
            // 写入 EOF 标记
            writeFileFooter();
            
            logger.info("Write JDB snapshot completed, total keys: {}", data.size());
        } catch (Exception e) {
            logger.error("Write JDB snapshot failed", e);
            throw e;
        }
    }

    /**
     *  For JodisString
     * |-----|-----|------|------|----------|
     * |kLen | key | vLen | value| expireTime|
     * |-----|-----|------|------|----------|
     *
     * @param key
     * @param jodisObject
     * @return
     */
    public ByteBuffer writeString(String key, JodisObject jodisObject) {
        JodisString value = (JodisString) jodisObject.getValue();
        long expireTime = jodisObject.getExpireTime();  // 获取 TTL 信息
        
        String valueString = value.getHolder();
        byte[] keys = ByteUtils.stringBytesWithLen(key);
        byte[] values = ByteUtils.stringBytesWithLen(valueString);
        int keysLen =  keys.length;
        int valuesLen = values.length;
        // 增加 8 字节存储 expireTime
        int len = JdbConstant.JDB_TYPE_SIZE + JdbConstant.JDB_INT_SIZE + keysLen + valuesLen + JdbConstant.JDB_LONG_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(len);

        buffer.put(JdbConstant.JDB_TYPE_STRING);
        buffer.putInt(len);
        buffer.put(keys);
        buffer.put(values);
        buffer.putLong(expireTime);  // 保存过期时间

        return buffer;
    }

    /**
     *  For JodisList
     * |-----|-----|-------|-------|------|-------|----------|
     * |kLen | key |listLen|itemLen| item |  ...  |expireTime|
     * |-----|-----|-------|-------|------|-------|----------|
     * @param key
     * @param jodisObject
     * @return
     */
    public ByteBuffer writeList(String key, JodisObject jodisObject) {
        JodisList value = (JodisList) jodisObject.getValue();
        List<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_LIST, jodisObject.getExpireTime());
    }

    /**
     *  For JodisHash
     * |-----|-----|-------|--------|-------|--------|-------|------|----------|
     * |kLen | key |hashLen|filedLen| filed |valueLen| value | ...  |expireTime|
     * |-----|-----|-------|--------|-------|--------|-------|------|----------|
     * @param key
     * @param jodisObject
     * @return
     */
    public ByteBuffer writeHash(String key, JodisObject jodisObject) {
        JodisHash value = (JodisHash) jodisObject.getValue();
        Map<String, String> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.keySet());
        kvs.addAll(values.values());
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_HASH, jodisObject.getExpireTime());
    }

    /**
     *  For JodisSet
     * |-----|-----|-------|-------|------|-------|----------|
     * |kLen | key | setLen|itemLen| item |  ...  |expireTime|
     * |-----|-----|-------|-------|------|-------|----------|
     * @param key
     * @param jodisObject
     * @return
     */
    public ByteBuffer writeSet(String key, JodisObject jodisObject) {
        JodisSet value = (JodisSet) jodisObject.getValue();
        Set<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_SET, jodisObject.getExpireTime());
    }

    /**
     *  For JodisSortedSet
     * |-----|-----|-------|--------|-------|--------|-------|------|----------|
     * |kLen | key |hashLen|filedLen| filed |scoreLen| score | ...  |expireTime|
     * |-----|-----|-------|--------|-------|--------|-------|------|----------|
     *
     * @param key
     * @param jodisObject
     * @return
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
        // 集合长度
        totalLen += JdbConstant.JDB_INT_SIZE;
        // 标志位长，总长度，expireTime
        totalLen += JdbConstant.JDB_TYPE_SIZE  + JdbConstant.JDB_INT_SIZE + JdbConstant.JDB_LONG_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        // 类型
        buffer.put(jdbType);
        //总长
        buffer.putInt(totalLen);
        //键长
        buffer.put(keys);
        buffer.putInt(value.size());
        for (byte[] item : valuesBytes) {
            buffer.put(item);
        }
        buffer.putLong(expireTime);  // 保存过期时间
        return buffer;
    }
    
    /**
     * 关闭资源
     */
    public void close() throws IOException {
        if (!closed) {
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
            closed = true;
        }
    }
}
