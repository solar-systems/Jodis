package cn.abelib.jodis.impl;

import cn.abelib.jodis.utils.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:14
 */
public class StringOperation extends KeyOperation{

    public StringOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisString getJodisString(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisString)jodisObject.getValue();
    }

    /**
     * Redis command: PUT
     * @param key
     * @param value
     * @return
     */
    public int put(String key, String value) {
        jodisCollection.put(key, JodisObject.putJodisString(value));
        return value.length();
    }

    /**
     * Redis command: GET
     * @param key
     * @return
     */
    public String get(String key) {
        if (!exists(key)) {
            return null;
        }
        return getJodisString(key).getHolder();
    }

    /**
     * Redis command: GETRANGE
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String getRange(String key, int start, int end) {
        if (start > end || !exists(key)) {
            return StringUtils.EMPTY;
        }
        String value = get(key);
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        }
        int len = value.length();
        if (start >= len || end < 0 ) {
            return StringUtils.EMPTY;
        }
        end = end >= len ? len - 1 : end;
        start = start < 0 ? 0 : start;
        return value.substring(start, end + 1);
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

    /**
     * todo
     * @param keys
     * @return
     */
    public List<String> multiGet(String ... keys) {
        return null;
    }
}
