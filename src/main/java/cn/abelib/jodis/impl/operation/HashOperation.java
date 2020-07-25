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

    public JodisHash getJodisMap(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisHash)jodisObject.getValue();
    }

    /**
     * Redis command: HGET
     * @param key
     * @return
     */
    public String hashGet(String key, String field){
        if (!exists(key)) {
            return null;
        }
        Map<String, String> map = getJodisMap(key).getHolder();
        return map.get(field);
    }

    /**
     * Redis command: HSET
     * @param key
     * @return
     */
    public int hashSet(String key, String field, String value){
        Map<String, String> map;
        if (exists(key)) {
            map = getJodisMap(key).getHolder();
        } else {
            map = new HashMap<>(8);
        }

        if (Objects.isNull(map.put(field, value))) {
            jodisCollection.put(key, JodisObject.putJodisHash(map));
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: HEXISTS
     * @param key
     * @param field
     * @return
     */
    public boolean hashExists(String key, String field) {
        if (!exists(key)) {
            return false;
        }
        Map<String, String> map = getJodisMap(key).getHolder();
        return map.containsKey(field);
    }

    /**
     * Redis command: HKEYS
     * @param key
     * @return
     */
    public Set<String> hashKeys(String key) {
        if (!exists(key)) {
            return null;
        }
        Map<String, String> map = getJodisMap(key).getHolder();
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
        if (!exists(key)) {
            return null;
        }
        Map<String, String> map = getJodisMap(key).getHolder();
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
       JodisHash map = getJodisMap(key);
        if (Objects.isNull(map)) {
            return 0;
        }
        return getJodisMap(key).size();
    }

    /**
     * Redis command: HSETNX
     * @param key
     * @param field
     * @param value
     * @return
     */
    public int hashSetIfNotExists(String key, String field, String value){
        if (hashExists(key, field)) {
            return 0;
        }

        return hashSet(key, field, value);
    }
}
