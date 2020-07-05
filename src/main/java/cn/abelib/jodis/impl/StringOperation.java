package cn.abelib.jodis.impl;

import cn.abelib.jodis.Jodis;
import cn.abelib.jodis.utils.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:14
 */
public class StringOperation extends KeyOperation{

    public StringOperation(Jodis jodis) {
        super(jodis);
    }

    /**
     * Redis command: PUT
     * @param key
     * @param value
     * @return
     */
    public int put(String key, String value) {
        JodisString jodisString = new JodisString(value);
        jodisCollection.put(key, jodisString);
        return jodisString.getLen();
    }

    /**
     * Redis command: GET
     * @param key
     * @return
     */
    public String get(String key) {
        JodisString value = (JodisString) jodisCollection.get(key);
        return value.getHolder();
    }

    /**
     * Redis command: GETRANGE
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String getRange(String key, int start, int end) {
        if (start > end) {
            return StringUtils.EMPTY;
        }
        JodisString value = (JodisString) jodisCollection.get(key);
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        }
        String holder = value.getHolder();
        int len = value.getLen();
        if (start >= len || end < 0 ) {
            return StringUtils.EMPTY;
        }
        end = end >= len ? len - 1 : end;
        start = start < 0 ? 0 : start;
        return holder.substring(start, end + 1);
    }

    /**
     * Redis command: GETSET
     * @param key
     * @param value
     * @return
     */
    public String getAndSet(String key, String value) {
        String old = get(key);
        put(key, value);
        return old;
    }

    /**
     * Redis command: SETNX
     * @param key
     * @param value
     * @return
     */
    public int setIfNotExists(String key, String value) {
        if (exists(key)) {
            return 1;
        }
        put(key, value);
        return 0;
    }

    /**
     * Redis command: SETNX
     * @param key
     * @return
     */
    public int strLen(String key) {
        if (!exists(key)) {
            return 0;
        }
        return get(key).length();
    }

    public List<String> multiGet(String ... keys) {
        return null;
    }
}
