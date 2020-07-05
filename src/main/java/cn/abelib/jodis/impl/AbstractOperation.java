package cn.abelib.jodis.impl;

import cn.abelib.jodis.Jodis;
import cn.abelib.jodis.api.JodisObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:20
 */
public class AbstractOperation {
    protected ConcurrentHashMap<String, JodisObject> jodisCollection;
    /**
     * 存储带过期时间的key
     */
    protected ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    public AbstractOperation(Jodis jodis) {
        this.jodisCollection = jodis.jodisCollection();
        this.expireJodisCollection = jodis.expireJodisCollection();
    }
}
