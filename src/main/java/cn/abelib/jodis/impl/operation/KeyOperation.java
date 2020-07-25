package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class KeyOperation extends AbstractOperation {

    public KeyOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisObject jodisObject(String key) {
        return jodisCollection.get(key);
    }

    /**
     * todo
     * 校验数据类型是否合法
     * @param key
     * @param type
     * @return
     */
    public boolean matchType(String key, String type) {
        JodisObject jodisObject = jodisObject(key);
        return type.equals(jodisObject.type());
    }

    public int size() {
        return jodisCollection.size();
    }

    /**
     * Redis command: TYPE
     * @param key
     * @return
     */
    public String type(String key) {
        if (!exists(key)) {
            return null;
        }
        return jodisObject(key).type();
    }

    /**
     * Redis command: DEL
     * @param key
     * @return
     */
    public void delete(String key) {
        jodisCollection.remove(key);
    }

    /**
     * Redis command: DUMP
     * @param key
     * @return
     */
    public String dump(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Redis command: EXISTS
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return jodisCollection.containsKey(key);
    }

    /**
     * todo TTL
     * @param key
     * @return
     */
    public long expire(String key, long timestamp, TimeUnit unit) {
        return 0L;
    }

    /**
     * todo TTL
     * @param key
     * @return
     */
    public long expireAt(String key, long timestamp, TimeUnit unit) {
        return 0L;
    }


    /**
     * Redis command: KEYS
     * @param pattern
     * @return
     */
    public List<String> keys(String pattern) {
        if (StringUtils.equals(OperationConstants.WILD_CARD_START, pattern)) {
            return  new ArrayList<>(jodisCollection.keySet());
        }
        return jodisCollection.keySet()
                .stream()
                .filter(key -> key.startsWith(pattern))
                .collect(Collectors.toList());
    }

    /**
     * Redis command: PERSIST
     * @param key
     */
    public void persist(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * todo
     * @param key
     * @param unit
     * @return
     */
    public long ttl(String key, TimeUnit unit) {
        return 0L;
    }

    /**
     * Redis command: RENAME
     * @param key
     * @param newKey
     */
    public boolean rename(String key, String newKey) {
        if (StringUtils.equals(key, newKey) || !exists(key)) {
            return false;
        }
        JodisObject value = jodisObject(key);
        delete(key);
        jodisCollection.put(newKey, value);
        return true;
    }

    /**
     * Redis command: RENAMENX
     * @param key
     * @param newKey
     */
    public boolean renameIfNotExist(String key, String newKey) {
        if (exists(newKey)) {
            return false;
        }
        return rename(key, newKey);
    }

    /**
     * Redis command: RANDOMKEY
     * @return
     */
    public String randomKey(){
        if (size() > 0) {
            return jodisCollection.keys().nextElement();
        }
        return null;
    }
}
