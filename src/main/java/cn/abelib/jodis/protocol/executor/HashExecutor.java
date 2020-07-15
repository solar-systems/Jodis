package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.HashOperation;
import cn.abelib.jodis.protocol.RespCmd;

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
    public RespCmd execute(String cmd, List<String> params) {
        return null;
    }
}
