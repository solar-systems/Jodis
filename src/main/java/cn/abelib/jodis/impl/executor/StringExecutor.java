package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.StringOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;

import java.util.List;
import java.util.Objects;

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
        int argSize = arguments.size();
        if (argSize < 1) {
            return ErrorResponse.errorArgsNum(command);
        }
        String key = arguments.get(0);
        if (StringUtils.isEmpty(key)) {
            return ErrorResponse.errorArgsNum(command);
        }
        String type = stringOperation.type(key);
        // 类型不匹配
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_STRING)) {
            return ErrorResponse.errorSyntax();
        }
        Integer num;
        Float numFloat;
        Integer start;
        Integer end;
        int timeout;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.STRING_SET:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                stringOperation.set(key, arguments.get(1));
                return SimpleResponse.ok();

             case ProtocolConstant.STRING_GET:
                 if (argSize != 1) {
                     return ErrorResponse.errorArgsNum(command, 1, argSize);
                 }
                 res = stringOperation.get(key);
                 return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_GETSET:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                res = stringOperation.getAndSet(key, arguments.get(1));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_GETRANGE:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                start = NumberUtils.toInt(key);
                if (Objects.isNull(start)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                end = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(end)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                res = stringOperation.getRange(command, start, end);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.STRING_MGET:
                list = stringOperation.multiGet(arguments);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.STRING_MSET:
                if (argSize % 2 != 0) {
                    return ErrorResponse.errorArgsNum(command, argSize + 1, argSize);
                }

                stringOperation.multiSet(arguments);
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETEX:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                timeout = NumberUtils.toInt(arguments.get(1));
                stringOperation.setExpire(key, timeout, arguments.get(2));
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETNX:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                stringOperation.setIfNotExists(key, arguments.get(1));
                return SimpleResponse.ok();

            case ProtocolConstant.STRING_SETRANGE:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                num = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(num)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = stringOperation.setRange(key, num, arguments.get(2));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_STRLEN:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = stringOperation.strLen(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCR:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = stringOperation.increment(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCRBY:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(num)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = stringOperation.incrementBy(key, num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_INCRBYFLOAT:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                numFloat = NumberUtils.toFloat(arguments.get(1));
                if (Objects.isNull(numFloat)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                numFloat = stringOperation.incrementByFloat(key, numFloat);
                return NumericResponse.numericResponse(numFloat);

            case ProtocolConstant.STRING_DECR:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = stringOperation.decrement(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_DECRBY:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(num)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = stringOperation.decrementBy(key, num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.STRING_APPEND:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = stringOperation.append(key, arguments.get(1));
                return NumericResponse.numericResponse(num);

            default:

        }
        return ErrorResponse.errorUnknownCmd(command);
    }
}
