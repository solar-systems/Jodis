package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.ByteUtils;

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
    public byte[] writeString(String key, JodisString value) {
        String valueString = value.getHolder();
        byte[] keys = ByteUtils.stringBytesWithLen(key);
        byte[] values = ByteUtils.stringBytesWithLen(valueString);
        int keysLen =  keys.length;
        int valuesLen = values.length;
        int len = JdbConstant.JDB_TYPE_SIZE + JdbConstant.JDB_INT_SIZE + keysLen + valuesLen;
        byte[] bytes = new byte[len];
        int idx = 0;
        bytes[0] = JdbConstant.JDB_TYPE_STRING;
        idx += JdbConstant.JDB_TYPE_SIZE;

        System.arraycopy(ByteUtils.int2Bytes(len), 0, bytes, idx, JdbConstant.JDB_INT_SIZE);
        idx += JdbConstant.JDB_INT_SIZE;

        System.arraycopy(keys, 0, bytes, idx, keysLen);
        idx += keysLen;

        System.arraycopy(values, 0, bytes, idx, valuesLen);
        return bytes;
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
    public byte[] writeList(String key, JodisList value) {
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
    public byte[] writeHash(String key, JodisHash value) {
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
    public byte[] writeSet(String key, JodisSet value) {
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
    public byte[] writeZSet(String key, JodisSortedSet value) {
        Map<String, Double> values = value.getHolder();
        List<String> kvs = new ArrayList<>(values.size() * 2);
        values.forEach((k, v) -> {
            kvs.add(k);
            kvs.add(String.valueOf(v));
        });
        return writeCollection(key, kvs, JdbConstant.JDB_TYPE_ZSET);
    }

    private byte[] writeCollection(String key, Collection<String> value, byte jdbType) {
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

        byte[] bytes = new byte[totalLen];
        int idx = 0;
        // 类型
        bytes[0] = jdbType;
        idx += JdbConstant.JDB_TYPE_SIZE;

        //总长
        System.arraycopy(ByteUtils.int2Bytes(totalLen), 0, bytes, idx, JdbConstant.JDB_INT_SIZE);
        idx += JdbConstant.JDB_INT_SIZE;

        //键长
        System.arraycopy(keys, 0, bytes, idx, keysLen);
        idx += keysLen;

        System.arraycopy(ByteUtils.int2Bytes(value.size()), 0, bytes, idx, JdbConstant.JDB_INT_SIZE);
        idx += JdbConstant.JDB_INT_SIZE;

        for (byte[] item : valuesBytes) {
            System.arraycopy(item, 0, bytes, idx, item.length);
            idx += item.length;
        }
        return bytes;
    }
}
