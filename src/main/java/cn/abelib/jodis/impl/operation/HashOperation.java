package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisHash;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
            return StringUtils.NIL;
        }
        String value = map.get(field);
        return StringUtils.isEmpty(value) ? StringUtils.NIL : value;
    }

    /**
     * Redis command: HGETALL
     * @param key
     * @return
     */
    public List<String> hashGetAll(String key){
        Map<String, String> map = getHash(key);
        List<String> list = Lists.newArrayList();
        if (Objects.isNull(map)) {
            return list;
        }
        map.forEach((k, v) -> {
            list.add(k);
            list.add(v);
        });
        return list;
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
    public List<String> hashKeys(String key) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(map.keySet()) ;
    }

    /**
     * Redis command: HVALS
     * @param key
     * @return
     */
    public List<String> hashValues(String key) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(map.values());
    }

    /**
     * Redis command: HLEN
     * @param key
     * @return
     */
    public int hashLen(String key) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return 0;
        }
        return map.size();
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
            map = Maps.newHashMap();
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
        int deletion = Objects.isNull(map.remove(filed)) ? 0 : 1 ;
        if (map.isEmpty()) {
            delete(key);
        }
        return deletion;
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

    /**
     * Redis command: HMSET
     * @param key
     * @param fieldValues
     * @return
     */
    public boolean hashMultiSet(String key, List<String> fieldValues) {
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            map = new HashMap<>(8);
            this.jodisDb.put(key, JodisObject.putJodisHash(map));
        }
        for (int i = 0; i < fieldValues.size(); i += 2) {
            map.put(fieldValues.get(i), fieldValues.get(i + 1));
        }
        return true;
    }

    /**
     *  Redis command: HMGET
     * @param key
     * @param fields
     * @return
     */
    public List<String> hashMultiGet(String key, List<String> fields) {
        List<String> ans =  Lists.newArrayList();
        Map<String, String> map = getHash(key);
        if (Objects.isNull(map)) {
            return ans;
        }
        fields.forEach(field -> {
            String value = map.get(field);
            if (StringUtils.isEmpty(value)) {
                ans.add(StringUtils.NIL);
            } else {
                ans.add(value);
            }
        });
        return ans;
    }
}
