package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.HashOperation;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:35
 */
public class HashExecutor implements Executor {
    private JodisDb jodisDb;
    private HashOperation hashOperation;

    public HashExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.hashOperation = new HashOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        return null;
    }
}
