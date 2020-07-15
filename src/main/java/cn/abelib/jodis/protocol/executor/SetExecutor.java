package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.SetOperation;
import cn.abelib.jodis.protocol.RespCmd;

import java.util.List;

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
    public RespCmd execute(String cmd, List<String> params) {
        return null;
    }
}
