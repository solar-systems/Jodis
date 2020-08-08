package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.SortedSetOperation;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class SortedSetExecutor implements Executor {
    private JodisDb jodisDb;
    private SortedSetOperation sortedSetOperation;

    public SortedSetExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.sortedSetOperation = new SortedSetOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        return null;
    }
}
