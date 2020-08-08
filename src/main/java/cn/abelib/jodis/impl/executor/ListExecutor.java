package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.ListOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.Utils;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class ListExecutor implements Executor {
    private ListOperation listOperation;

    public ListExecutor(JodisDb jodisDb) {
        this.listOperation = new ListOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argNum = arguments.size();
        int num;
        int start;
        int end;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.LIST_LINDEX:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = Utils.toInt(arguments.get(1));
                res = listOperation.leftIndex(arguments.get(0), num);
                return SimpleResponse.simpleResponse(res);

            /**
             * todo
             */
            case ProtocolConstant.LIST_LINSERT:
                if (argNum != 4) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                res = arguments.get(2);
                if (StringUtils.equals(res, "BEFORE")) {
                    num = listOperation.leftInsert(arguments.get(0), arguments.get(1), arguments.get(3));
                } else if (StringUtils.equals(res, "AFTER")) {
                    num = listOperation.leftInsert(arguments.get(0), arguments.get(1), arguments.get(3));
                } else {
                    return ErrorResponse.errorSyntax();
                }
                return NumericResponse.numericResponse(num);

            default:
        }
        return ErrorResponse.errorUnknownCmd(command);
    }
}
