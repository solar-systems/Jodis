package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:20
 */
public class AbstractOperation {
    protected JodisDb jodisDb;

    public AbstractOperation(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }
}
