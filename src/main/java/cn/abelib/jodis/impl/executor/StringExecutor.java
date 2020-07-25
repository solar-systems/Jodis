package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.StringOperation;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.ErrorResponse;
import cn.abelib.jodis.protocol.Response;
import cn.abelib.jodis.protocol.SimpleResponse;

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
    public Response execute(String cmd, List<String> params) {
        switch (cmd) {
            case ProtocolConstant.STRING_SET:
                if (params.size() == 2) {
                   stringOperation.set(params.get(0), params.get(1));
                   return SimpleResponse.ok();
                } else {
                    return ErrorResponse.errorSyntax();
                }
             case ProtocolConstant.STRING_GET:
                 if (params.size() == 1) {
                     String result = stringOperation.get(params.get(0));
                     return SimpleResponse.result(result);
                 } else {
                     return ErrorResponse.errorSyntax();
                 }

             default:

        }
        return ErrorResponse.error("");
    }
}
