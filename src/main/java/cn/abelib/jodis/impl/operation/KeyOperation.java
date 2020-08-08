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
        return this.jodisDb.get(key);
    }

    public int size() {
        return jodisDb.size();
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
    public int delete(String key) {
        if (this.jodisDb.containsKey(key)) {
            this.jodisDb.remove(key);
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: EXISTS
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return this.jodisDb.containsKey(key);
    }

    /**
     * todo TTL
     * @param key
     * @return
     */
    public int expire(String key, long timestamp, TimeUnit unit) {
        return 0;
    }

    /**
     * todo TTL
     * @param key
     * @return
     */
    public int expireAt(String key, long timestamp, TimeUnit unit) {
        return 0;
    }

    /**
     * Redis command: KEYS
     * @param pattern
     * @return
     */
    public List<String> keys(String pattern) {
        if (StringUtils.equals(OperationConstants.WILD_CARD_START, pattern)) {
            return new ArrayList<>(this.jodisDb.keySet());
        }
        return this.jodisDb.keySet()
                .stream()
                .filter(key -> key.startsWith(pattern))
                .collect(Collectors.toList());
    }

    /**
     * todo
     * @param key
     * @param unit
     * @return
     */
    public int ttl(String key, long timestamp, TimeUnit unit) {
        return 0;
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
        this.jodisDb.put(newKey, value);
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
            return this.jodisDb.keySet().iterator().next();
        }
        return null;
    }
}
