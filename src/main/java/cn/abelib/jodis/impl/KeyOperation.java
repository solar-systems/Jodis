package cn.abelib.jodis.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class KeyOperation extends AbstractOperation{

    public KeyOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public int size() {
        return jodisCollection.size();
    }

    /**
     * 删除Key
     * @param key
     * @return
     */
    public boolean delete(String key) {
        jodisCollection.remove(key);
        return true;
    }

    public boolean exists(String key) {
        return jodisCollection.containsKey(key);
    }

    /**
     * todo
     * @param key
     * @return
     */
    public long expire(String key, long timestamp, TimeUnit unit) {
        return 0L;
    }

    /**
     * todo
     * @param pattern
     * @return
     */
    public List<String> keys(String pattern) {
        return null;
    }

    /**
     * todo
     * @param key
     */
    public void persist(String key) {

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
     * todo
     * @param key
     * @param newKey
     */
    public void rename(String key, String newKey) {

    }
    /**
     * todo
     * @param key
     * @param newKey
     */
    public void renameIfNotExist(String key, String newKey) {

    }

    /**
     * todo
     * @param key
     * @return
     */
    public String type(String key){
        return null;
    }
}
