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

            case ProtocolConstant.LIST_LINSERT:
                if (argNum != 4) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                res = arguments.get(2);
                if (StringUtils.equals(res, "BEFORE")) {
                    num = listOperation.leftInsert(arguments.get(0), arguments.get(1), arguments.get(3));
                } else if (StringUtils.equals(res, "AFTER")) {
                    num = listOperation.rightInsert(arguments.get(0), arguments.get(1), arguments.get(3));
                } else {
                    return ErrorResponse.errorSyntax();
                }
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_LPOP:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                res = listOperation.leftPop(arguments.get(0));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.LIST_RPOP:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                res = listOperation.rightPop(arguments.get(0));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.LIST_LPUSH:
                if (argNum < 1) {
                    return ErrorResponse.errorArgsNum(command);
                }
                res = arguments.get(0);
                arguments.remove(0);
                num = listOperation.leftPush(res, arguments);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_RPUSH:
                if (argNum < 1) {
                    return ErrorResponse.errorArgsNum(command);
                }
                res = arguments.get(0);
                arguments.remove(0);
                num = listOperation.rightPush(res, arguments);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_LRANGE:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                start = Utils.toInt(arguments.get(1));
                end = Utils.toInt(arguments.get(2));
                list = listOperation.listRange(arguments.get(0), start, end);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.LIST_LREM:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                num = Utils.toInt(arguments.get(1));
                num = listOperation.listRemove(arguments.get(0), num, arguments.get(2));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_LTRIM:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                start = Utils.toInt(arguments.get(1));
                end = Utils.toInt(arguments.get(2));
                listOperation.leftTrim(arguments.get(0), start, end);
                return SimpleResponse.ok();

            default:
        }
        return ErrorResponse.errorUnknownCmd(command);
    }
}
