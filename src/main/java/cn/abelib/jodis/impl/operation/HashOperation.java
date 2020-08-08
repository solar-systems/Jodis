package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisHash;
import cn.abelib.jodis.impl.JodisObject;

import java.util.*;

/**
 * @Author: abel.huang
 * @Date: 2020-07-12 18:18
 */
public class HashOperation extends KeyOperation {

    public HashOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisHash getJodisHash(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisHash)jodisObject.getValue();
    }

    /**
     * Redis command: HGETALL
     * @param key
     * @return
     */
    public Map<String, String> getHash(String key) {
        if (exists(key)) {
            return getJodisHash(key).getHolder();
        }
        return null;
    }

    /**
     * Redis command: HGET
     * @param key
     * @return
     */
    public String hashGet(String key, String field){
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return null;
        }

        return map.get(field);
    }

    /**
     * Redis command: HSET
     * @param key
     * @return
     */
    public boolean hashSet(String key, String field, String value){
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            map = new HashMap<>(8);
            this.jodisDb.put(key, JodisObject.putJodisHash(map));
        }
        return Objects.isNull(map.put(field, value));
    }

    /**
     * Redis command: HEXISTS
     * @param key
     * @param field
     * @return
     */
    public boolean hashExists(String key, String field) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return false;
        }
        return map.containsKey(field);
    }

    /**
     * Redis command: HKEYS
     * @param key
     * @return
     */
    public Set<String> hashKeys(String key) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return null;
        }
        return map.keySet();
    }

    /**
     * Redis command: HVALS
     * @param key
     * @return
     */
    public Collection<String> hashValues(String key) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return null;
        }
        return map.values();
    }

    /**
     * Redis command: HLEN
     * @param key
     * @return
     */
    public int hashLen(String key) {
        if (!exists(key)) {
            return 0;
        }
        return getJodisHash(key).size();
    }

    /**
     * Redis command: HSETNX
     * @param key
     * @param field
     * @param value
     * @return
     */
    public boolean hashSetIfNotExists(String key, String field, String value){
        if (hashExists(key, field)) {
            return true;
        }

        return hashSet(key, field, value);
    }

    /**
     * Redis command: HINCRBY
     * @param key
     * @param filed
     * @param incrNumber
     * @return
     */
    public int hashIncrementBy(String key, String filed, int incrNumber) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            map = new HashMap<>(8);
            this.jodisDb.put(key, JodisObject.putJodisHash(map));
        }
        String value = map.get(filed);
        int res = incrNumber;
        if (Objects.isNull(value)) {
            map.put(filed, String.valueOf(incrNumber));
        } else {
            res += Integer.parseInt(value);
            map.put(filed, String.valueOf(res));

        }
        return res;
    }

    /**
     * Redis command: HDEL
     * @param key
     * @param filed
     * @return
     */
    public int hashDelete(String key, String filed) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return 0;
        }
        return Objects.isNull(map.remove(filed)) ? 0 : 1 ;
    }

    /**
     * Redis command: HINCRBY
     * @param key
     * @param filed
     * @param incrNumber
     * @return
     */
    public float hashIncrementByFloat(String key, String filed, float incrNumber) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            map = new HashMap<>(8);
            this.jodisDb.put(key, JodisObject.putJodisHash(map));
        }
        String value = map.get(filed);
        float res = incrNumber;
        if (Objects.isNull(value)) {
            map.put(filed, String.valueOf(incrNumber));
        } else {
            res += Float.parseFloat(value);
            map.put(filed, String.valueOf(res));

        }
        return res;
    }
}
