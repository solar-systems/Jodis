package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.ListOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;

import java.util.List;
import java.util.Objects;

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
        int argSize = arguments.size();
        if (argSize < 1) {
            return ErrorResponse.errorArgsNum(command);
        }
        String key = arguments.get(0);
        if (StringUtils.isEmpty(key)) {
            return ErrorResponse.errorArgsNum(command);
        }
        String type = listOperation.type(key);
        // 类型不匹配
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_LIST)) {
            return ErrorResponse.errorSyntax();
        }
        Integer num;
        Integer start;
        Integer end;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.LIST_LINDEX:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(num)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                res = listOperation.leftIndex(key, num);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.LIST_LINSERT:
                if (argSize != 4) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                res = arguments.get(2);
                if (StringUtils.equalsIgnoreCase(res, ProtocolConstant.LIST_BEFORE)) {
                    num = listOperation.leftInsert(key, arguments.get(1), arguments.get(3));
                } else if (StringUtils.equalsIgnoreCase(res, ProtocolConstant.LIST_AFTER)) {
                    num = listOperation.rightInsert(key, arguments.get(1), arguments.get(3));
                } else {
                    return ErrorResponse.errorSyntax();
                }
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_LPOP:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                res = listOperation.leftPop(key);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.LIST_RPOP:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                res = listOperation.rightPop(key);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.LIST_LPUSH:
                if (argSize < 2) {
                    return ErrorResponse.errorArgsNum(command);
                }
                arguments.remove(0);
                num = listOperation.leftPush(key, arguments);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_RPUSH:
                if (argSize < 2) {
                    return ErrorResponse.errorArgsNum(command);
                }
                arguments.remove(0);
                num = listOperation.rightPush(key, arguments);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.LIST_LRANGE:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                start = NumberUtils.toInt(arguments.get(1));
                if (Objects.isNull(start)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                end = NumberUtils.toInt(arguments.get(2));
                if (Objects.isNull(end)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                list = listOperation.listRange(key, start, end);
                return ListResponse.stringListResponse(list);

            default:
        }
        return ErrorResponse.errorUnknownCmd(command);
    }
}
