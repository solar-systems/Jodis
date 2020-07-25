package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.ListOperation;
import cn.abelib.jodis.protocol.Response;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class ListExecutor implements Executor {
    private JodisDb jodisDb;
    private ListOperation listOperation;

    public ListExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.listOperation = new ListOperation(jodisDb);
    }

    @Override
    public Response execute(String cmd, List<String> params) {
        return null;
    }
}
