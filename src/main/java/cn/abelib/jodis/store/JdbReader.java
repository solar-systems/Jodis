package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.impl.SkipList;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.KeyValue;
import cn.abelib.jodis.utils.Logger;
import com.google.common.collect.Sets;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;


/**
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 * Jdb means Jodis Database
 */
public class JdbReader {
    private Logger logger = Logger.getLogger(JdbReader.class);
    private Path jdbFile;

    public JdbReader() {

    }

    public JdbReader(String dir, String fName) throws IOException {
        this.jdbFile = IoUtils.createFileIfNotExists(dir, fName);
    }
    
    /**
     * 读取快照文件，返回所有数据
     * @return Map<String, JodisObject>
     * @throws IOException
     */
    public Map<String, JodisObject> readSnapshot() throws IOException {
        Map<String, JodisObject> data = new HashMap<>();
        long now = System.currentTimeMillis();
        
        try (DataInputStream dis = new DataInputStream(new FileInputStream(jdbFile.toFile()))) {
            // 1. 验证文件头
            int magicLen = dis.readByte();
            byte[] magicBytes = new byte[magicLen];
            dis.readFully(magicBytes);
            String magic = new String(magicBytes);
            
            if (!JdbConstant.JDB_MAGIC_FLAG.equals(magic)) {
                throw new IOException("Invalid JDB file: bad magic flag '" + magic + "', expected '" + JdbConstant.JDB_MAGIC_FLAG + "'");
            }
            
            int versionLen = dis.readByte();
            byte[] versionBytes = new byte[versionLen];
            dis.readFully(versionBytes);
            String version = new String(versionBytes);
            
            if (!"0001".equals(version)) {
                logger.warn("Unsupported JDB version: {}, but will try to read", version);
            }
            
            logger.info("Read JDB file started, version: {}", version);
            
            // 2. 读取所有 Entry
            int count = 0;
            int expiredCount = 0;
            
            while (true) {
                byte type = dis.readByte();
                if (type == JdbConstant.EOF) {
                    break;  // 文件结束
                }
                
                int totalLen = dis.readInt();
                byte[] entryData = new byte[totalLen - 1];  // 减去 type 占用的 1 字节
                dis.readFully(entryData);
                
                ByteBuffer buffer = ByteBuffer.wrap(entryData);
                KeyValue<String, JodisObject> entry = parseEntry(type, buffer);
                
                if (entry != null && !entry.isNull()) {
                    // 检查是否过期
                    if (entry.getValue().getExpireTime() > 0 && entry.getValue().getExpireTime() < now) {
                        expiredCount++;
                        logger.info("Skip expired key: {}", entry.getKey());
                    } else {
                        data.put(entry.getKey(), entry.getValue());
                        count++;
                    }
                }
            }
            
            logger.info("Read JDB file completed, total keys: {}, expired keys: {}", count, expiredCount);
            return data;
        }
    }
    
    /**
     * 解析 Entry 数据
     */
    private KeyValue<String, JodisObject> parseEntry(byte type, ByteBuffer buffer) {
        buffer.flip();
        
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
        if (Objects.isNull(buffer)) {
            return new KeyValue<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KeyValue<>();
        }
        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        int valueLen =  buffer.getInt();
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
        long expireTime = ans.getExpireTime();  // 获取 TTL
        
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
        long expireTime = ans.getExpireTime();  // 获取 TTL
        
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
        long expireTime = ans.getExpireTime();  // 获取 TTL
        
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
        long expireTime = ans.getExpireTime();  // 获取 TTL
        
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
        if (Objects.isNull(buffer)) {
            return new KeyValue<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KeyValue<>();
        }
        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        // 集合长度
        int len = buffer.getInt();
        List<String> value = new ArrayList<>(len);
        for (int i = 0; i < len ; i ++) {
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
}
