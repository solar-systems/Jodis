package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.ListOperation;
import cn.abelib.jodis.protocol.RespCmd;

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
    public RespCmd execute(String cmd, List<String> params) {
        return null;
    }
}
