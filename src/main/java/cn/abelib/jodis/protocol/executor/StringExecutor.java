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

    /**
     *  todo 考虑策略模式优化
     * 处理String相关的命令
     * @param cmd
     * @param params
     * @return
     */
    @Override
    public RespCmd execute(String cmd, List<String> params) {
        switch (cmd) {
            case CmdConstant.STRING_SET:
                if (params.size() == 2) {
                   stringOperation.set(params.get(0), params.get(1));
                   return SimpleCmd.ok();
                } else {
                    return ErrorCmd.errorSyntax();
                }
             case CmdConstant.STRING_GET:
                 if (params.size() == 1) {
                     String result = stringOperation.get(params.get(0));
                     return SimpleCmd.result(result);
                 } else {
                     return ErrorCmd.errorSyntax();
                 }

             default:

        }
        return ErrorCmd.error("");
    }
}
