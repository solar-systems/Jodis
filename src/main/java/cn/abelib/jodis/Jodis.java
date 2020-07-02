package cn.abelib.jodis;

import cn.abelib.jodis.impl.JodisKey;
import cn.abelib.jodis.impl.JodisString;
import cn.abelib.jodis.api.JodisObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 * Java Object Dictionary Server
 */
public class Jodis {
    private ConcurrentHashMap<JodisKey, JodisObject> jodisCollection;

    public Jodis() {
        jodisCollection = new ConcurrentHashMap<>();
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

    public int put(String key, String value) {
        JodisKey jodisKey = new JodisKey();
        jodisKey.setKey(key);
        JodisString jodisString = new JodisString(value);

        jodisCollection.put(jodisKey, jodisString);
        return 1;
    }

    public String get(String key) {
        JodisKey jodisKey = new JodisKey();
        jodisKey.setKey(key);
        JodisString value = (JodisString) jodisCollection.get(jodisKey);
        return value.getHolder();
    }
}
