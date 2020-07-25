package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;

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

    public AbstractOperation(JodisDb jodisDb) {
        this.jodisCollection = jodisDb.jodisCollection();
        this.expireJodisCollection = jodisDb.expireJodisCollection();
    }
}
