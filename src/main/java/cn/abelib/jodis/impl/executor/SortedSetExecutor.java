package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.SortedSetOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.Utils;

import java.util.List;

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
        int argNum = arguments.size();
        int num;
        double numDouble;
        double min;
        double max;
        switch (command) {
            case ProtocolConstant.ZSET_ZADD:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                numDouble =  Utils.toDouble(arguments.get(1));
                num = sortedSetOperation.zAdd(arguments.get(0), numDouble, arguments.get(2));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZCARD:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = sortedSetOperation.zCard(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZSCORE:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                numDouble = sortedSetOperation.zScore(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(numDouble);

            case ProtocolConstant.ZSET_ZCOUNT:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                min = Utils.toDouble(arguments.get(1));
                max = Utils.toDouble(arguments.get(2));
                num = sortedSetOperation.zCount(arguments.get(0), min, max);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.ZSET_ZREM:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = sortedSetOperation.zRemove(arguments.get(0),arguments.get(1));
                return NumericResponse.numericResponse(num);

            default:
                break;
        }
        return ErrorResponse.errorCommon();
    }
}
