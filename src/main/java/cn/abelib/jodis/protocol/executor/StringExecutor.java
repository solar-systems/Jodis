package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.StringOperation;
import cn.abelib.jodis.protocol.CmdConstant;
import cn.abelib.jodis.protocol.ErrorCmd;
import cn.abelib.jodis.protocol.RespCmd;
import cn.abelib.jodis.protocol.SimpleCmd;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 00:58
 */
public class StringExecutor implements Executor {
    private JodisDb jodisDb;
    private StringOperation stringOperation;

    public StringExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        this.stringOperation = new StringOperation(jodisDb);
    }

    // todo
    @Override
    public RespCmd execute(String cmd, List<String> params) {
        switch (cmd) {
            case CmdConstant.STRING_SET:
                if (params.size() == 2) {
                   int result =  stringOperation.set(params.get(0), params.get(1));
                   return new SimpleCmd("OK");
                }
                break;
             case CmdConstant.STRING_GET:


             default:

        }
        return new ErrorCmd();
    }
}
