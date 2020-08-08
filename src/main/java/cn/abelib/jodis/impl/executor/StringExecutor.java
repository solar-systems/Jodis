package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.StringOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.Utils;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 00:58
 */
public class StringExecutor implements Executor {
    private StringOperation stringOperation;

    public StringExecutor(JodisDb jodisDb) {
        this.stringOperation = new StringOperation(jodisDb);
    }

    /**
     *  todo 考虑策略模式优化
     * 处理String相关的命令
     * @param request
     * @return
     */
    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argNum = arguments.size();
        int num;
        float numFloat;
        int start;
        int end;
        int timeout;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.STRING_SET:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                stringOperation.set(arguments.get(0), arguments.get(1));
                return SimpleResponse.ok();

             case ProtocolConstant.STRING_GET:
                 if (argNum != 1) {
                     return ErrorResponse.errorArgsNum(command, 1, argNum);
                 }
                 res = stringOperation.get(arguments.get(0));
                 return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_GETSET:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                res = stringOperation.getAndSet(arguments.get(0), arguments.get(1));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_GETRANGE:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                start = Utils.toInt(arguments.get(0));
                end = Utils.toInt(arguments.get(1));
                res = stringOperation.getRange(command, start, end);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_MGET:
                if (argNum < 1) {
                    return ErrorResponse.errorArgsNum(command);
                }

                list = stringOperation.multiGet(arguments);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.STRING_MSET:
                if (argNum % 2 != 0) {
                    return ErrorResponse.errorArgsNum(command, argNum + 1, argNum);
                }

                stringOperation.multiSet(arguments);
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETEX:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                timeout = Utils.toInt(arguments.get(1));
                stringOperation.setExpire(arguments.get(0), timeout, arguments.get(2));
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETNX:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                stringOperation.setIfNotExists(arguments.get(0), arguments.get(1));
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETRANGE:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                num = Utils.toInt(arguments.get(1));
                num = stringOperation.setRange(arguments.get(0), num, arguments.get(2));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_STRLEN:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = stringOperation.strLen(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCR:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = stringOperation.increment(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCRBY:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = Utils.toInt(arguments.get(1));
                if (num == 0) {
                    return ErrorResponse.errorSyntax();
                }
                num = stringOperation.incrementBy(arguments.get(0), num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCRBYFLOAT:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                numFloat = Utils.toFloat(arguments.get(1));
                if (numFloat == 0) {
                    return ErrorResponse.errorSyntax();
                }
                numFloat = stringOperation.incrementByFloat(arguments.get(0), numFloat);
                return NumericResponse.numericResponse(numFloat);

            case ProtocolConstant.STRING_DECR:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = stringOperation.decrement(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_DECRBY:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = Utils.toInt(arguments.get(1));
                if (num == 0) {
                    return ErrorResponse.errorSyntax();
                }
                num = stringOperation.decrementBy(arguments.get(0), num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_APPEND:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = stringOperation.append(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(num);

            default:

        }
        return ErrorResponse.errorUnknownCmd(command);
    }
}
