package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.ZSetOperation;
import cn.abelib.jodis.protocol.RespCmd;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class ZSetExecutor implements Executor {
    private JodisDb jodisDb;
    private ZSetOperation zSetOperation;

    public ZSetExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.zSetOperation = new ZSetOperation(jodisDb);
    }

    @Override
    public RespCmd execute(String cmd, List<String> params) {
        return null;
    }
}
