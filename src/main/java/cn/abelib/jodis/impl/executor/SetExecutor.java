package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.SetOperation;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;


/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class SetExecutor implements Executor {
    private JodisDb jodisDb;
    private SetOperation setOperation;

    public SetExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.setOperation = new SetOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        return null;
    }
}
