package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.ByteUtils;

import java.nio.ByteBuffer;
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

    public JdbWriter() {

    }

    public byte[] write(JodisObject jodisObject) {
        return new byte[0];
    }

    /**
     *  For JodisString
     * |-----|-----|------|------|
     * |kLen | key | vLen | value|
     * |-----|-----|------|------|
     *
     * @param key
     * @param value
     * @return
     */
    public ByteBuffer writeString(String key, JodisString value) {
        String valueString = value.getHolder();
        byte[] keys = ByteUtils.stringBytesWithLen(key);
        byte[] values = ByteUtils.stringBytesWithLen(valueString);
        int keysLen =  keys.length;
        int valuesLen = values.length;
        int len = JdbConstant.JDB_TYPE_SIZE + JdbConstant.JDB_INT_SIZE + keysLen + valuesLen;

        ByteBuffer buffer = ByteBuffer.allocate(len);

        buffer.put(JdbConstant.JDB_TYPE_STRING);
        buffer.putInt(len);
        buffer.put(keys);
        buffer.put(values);

        return buffer;
    }

    /**
     *  For JodisList
     * |-----|-----|-------|-------|------|-------|
     * |kLen | key |listLen|itemLen| item |  ...  |
     * |-----|-----|-------|-------|------|-------|
     * @param key
     * @param value
     * @return
     */
    public ByteBuffer writeList(String key, JodisList value) {
        List<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_LIST);
    }

    /**
     *  For JodisHash
     * |-----|-----|-------|--------|-------|--------|-------|------|
     * |kLen | key |hashLen|filedLen| filed |valueLen| value | ...  |
     * |-----|-----|-------|--------|-------|--------|-------|------|
     * @param key
     * @param value
     * @return
     */
    public ByteBuffer writeHash(String key, JodisHash value) {
        Map<String, String> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.keySet());
        kvs.addAll(values.values());
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_HASH);
    }

    /**
     *  For JodisSet
     * |-----|-----|-------|-------|------|-------|
     * |kLen | key | setLen|itemLen| item |  ...  |
     * |-----|-----|-------|-------|------|-------|
     * @param key
     * @param value
     * @return
     */
    public ByteBuffer writeSet(String key, JodisSet value) {
        Set<String> values = value.getHolder();
        return writeCollection(key, values, JdbConstant.JDB_TYPE_SET);
    }

    /**
     *  For JodisSortedSet
     * |-----|-----|-------|--------|-------|--------|-------|------|
     * |kLen | key |hashLen|filedLen| filed |scoreLen| score | ...  |
     * |-----|-----|-------|--------|-------|--------|-------|------|
     *
     * @param key
     * @param value
     * @return
     */
    public ByteBuffer writeZSet(String key, JodisSortedSet value) {
        Map<String, Double> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.size() * 2);
        values.forEach((k, v) -> {
            kvs.add(k);
            kvs.add(String.valueOf(v));
        });
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_ZSET);
    }

    private ByteBuffer writeCollection(String key, Collection<String> value, byte jdbType) {
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
        // 标志位长， 总长度
        totalLen += JdbConstant.JDB_TYPE_SIZE  + JdbConstant.JDB_INT_SIZE ;

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
        return buffer;
    }
}
