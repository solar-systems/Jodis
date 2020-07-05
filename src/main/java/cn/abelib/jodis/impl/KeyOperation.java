package cn.abelib.jodis.impl;

import cn.abelib.jodis.Jodis;


/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class KeyOperation extends AbstractOperation{

    public KeyOperation(Jodis jodis) {
        super(jodis);
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
}
