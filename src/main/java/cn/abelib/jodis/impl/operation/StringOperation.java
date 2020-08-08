package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.impl.JodisString;
import cn.abelib.jodis.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:14
 */
public class StringOperation extends KeyOperation {

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
     * Redis command: SET
     * @param key
     * @param value
     * @return
     */
    public int set(String key, String value) {
        this.jodisDb.put(key, JodisObject.putJodisString(value));
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
     * 不支持负数(Redis是支持的)
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
     * todo
     * Redis command: SETRANGE
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public int setRange(String key, int offset, String value) {
        if (!exists(key)) {
            return 0;
        }
        if (offset < 0) {
            offset = 0;
        }

        return 0;
    }

    /**
     * todo
     * Redis command: GETSET
     * @param key
     * @param value
     * @return
     */
    public String getAndSet(String key, String value) {
        String old = get(key);
        set(key, value);
        return old;
    }

    /**
     * Redis command: SETNX
     * @param key
     * @param value
     * @return
     */
    public boolean setIfNotExists(String key, String value) {
        if (exists(key)) {
            return false;
        }
        set(key, value);
        return true;
    }

    /**
     * Redis command: STRLEN
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
     * Redis command: MGET
     * @param keys
     * @return
     */
    public List<String> multiGet(List<String> keys) {
        List<String> values = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            String value = get(key);
            values.add(StringUtils.isEmpty(value) ? StringUtils.NIL : value);
        });
        return values;
    }

    /**
     * Redis command: MSET
     * @param keyValues
     * @return
     */
    public void multiSet(List<String> keyValues) {
        int len = keyValues.size();
        for (int i = 0; i < len; i += 2) {
            set(keyValues.get(i), keyValues.get(i + 1));
        }
    }

    /**
     * Redis command: APPEND
     * @param key
     * @param value
     * @return
     */
    public int append(String key, String value) {
        if (!exists(key)) {
            return set(key, value);
        }
        String old = get(key);
        return set(key, old + value);
    }

    /**
     * Redis command: INCR
     * @param key
     * @return
     */
    public int increment(String key) {
        return incrementBy(key, 1);
    }

    /**
     * Redis command: INCRBY
     * @param key
     * @param incrAmount
     * @return
     */
    public int incrementBy(String key, int incrAmount) {
        if (!exists(key)) {
            set(key, String.valueOf(incrAmount));
            return incrAmount;
        }
        String value = get(key);
        int incr;
        try {
            incr = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
        incr += incrAmount;
        set(key, String.valueOf(incr));
        return incr;
    }

    /**
     * Redis command: INCRBYFLOAT
     * @param key
     * @param incrAmount
     * @return
     */
    public float incrementByFloat(String key, float incrAmount) {
        if (!exists(key)) {
            set(key, String.valueOf(incrAmount));
            return incrAmount;
        }
        String value = get(key);
        float incr;
        try {
            incr = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return -1;
        }
        incr += incrAmount;
        set(key, String.valueOf(incr));
        return incr;
    }

    /**
     * Redis command: DECR
     * @param key
     * @return
     */
    public int decrement(String key) {
        return decrementBy(key, 1);
    }

    /**
     * Redis command: DECRBY
     * @param key
     * @return
     */
    public int decrementBy(String key, int decrAmount) {
        int decr = 0 - decrAmount;
       return incrementBy(key, decr);
    }

    /**
     * todo
     * not support now
     * Redis command: SETEX
     * @param key
     * @param timeout
     * @param value
     * @return
     */
    public boolean setExpire(String key, int timeout, String value) {
        set(key, value);
        expire(key, timeout, TimeUnit.SECONDS);
        return true;
    }
}
