package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.SortedSetOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class SortedSetExecutor implements Executor {
    private SortedSetOperation sortedSetOperation;

    public SortedSetExecutor(JodisDb jodisDb) {
        this.sortedSetOperation = new SortedSetOperation(jodisDb);
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
        String type = sortedSetOperation.type(key);
        // 类型不匹配
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_ZSET)) {
            return ErrorResponse.errorSyntax();
        }
        int num;
        Double numDouble;
        Double min;
        Double max;
        switch (command) {
            case ProtocolConstant.ZSET_ZADD:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                numDouble =  NumberUtils.parseDouble(arguments.get(1));
                if (Objects.isNull(numDouble)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = sortedSetOperation.zAdd(key, numDouble, arguments.get(2));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZCARD:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = sortedSetOperation.zCard(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZSCORE:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                numDouble = sortedSetOperation.zScore(key, arguments.get(1));
                return NumericResponse.numericResponse(numDouble);

            case ProtocolConstant.ZSET_ZCOUNT:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                min = NumberUtils.parseDouble(arguments.get(1));
                if (Objects.isNull(min)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                max = NumberUtils.parseDouble(arguments.get(2));
                if (Objects.isNull(max)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = sortedSetOperation.zCount(key, min, max);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZREM:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = sortedSetOperation.zRemove(key,arguments.get(1));
                return NumericResponse.numericResponse(num);

            default:
                break;
        }
        return ErrorResponse.errorCommon();
    }
}
