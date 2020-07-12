package cn.abelib.jodis.impl;

import cn.abelib.jodis.protocol.RequestCommand;
import cn.abelib.jodis.protocol.RespCommand;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 */
public class JodisDb {
    /**
     * 存储不带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> jodisCollection;
    /**
     * 存储带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    public JodisDb() {
        jodisCollection = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, JodisObject> jodisCollection() {
        return this.jodisCollection;
    }

    public ConcurrentHashMap<String, JodisObject> expireJodisCollection() {
        return this.expireJodisCollection;
    }

    public JodisObject put(String key, JodisObject value) {
        return jodisCollection.put(key, value);
    }

    /**
     * 执行命令
     * @param request
     * @return
     */
    public RespCommand execute(RequestCommand request) {
        return null;
    }
}
