package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.HashOperation;
import cn.abelib.jodis.protocol.Response;

import java.util.List;

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
    public Response execute(String cmd, List<String> params) {
        return null;
    }
}
