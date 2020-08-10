package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.HashOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.Utils;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:35
 */
public class HashExecutor implements Executor {
    private HashOperation hashOperation;

    public HashExecutor(JodisDb jodisDb) {
        this.hashOperation = new HashOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argNum = arguments.size();
        int num;
        float numFloat;
        boolean flag;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.HASH_HDEL:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = hashOperation.hashDelete(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HEXISTS:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                flag = hashOperation.hashExists(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.HASH_HGET:
                if (argNum != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                res = hashOperation.hashGet(arguments.get(0), arguments.get(1));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.HASH_HGETALL:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                list = hashOperation.hashGetAll(arguments.get(0));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HINCRBY:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                num = Utils.toInt(arguments.get(2));
                if (num == 0) {
                    return ErrorResponse.errorSyntax();
                }
                num = hashOperation.hashIncrementBy(arguments.get(0), arguments.get(1), num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HINCRBYFLOAT:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                numFloat = Utils.toFloat(arguments.get(2));
                if (numFloat == 0) {
                    return ErrorResponse.errorSyntax();
                }
                numFloat = hashOperation.hashIncrementByFloat(arguments.get(0), arguments.get(1), numFloat);
                return NumericResponse.numericResponse(numFloat);

            case ProtocolConstant.HASH_HKEYS:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                list = hashOperation.hashKeys(arguments.get(0));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HVALS:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                list = hashOperation.hashValues(arguments.get(0));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HLEN:
                if (argNum != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = hashOperation.hashLen(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HSET:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                flag = hashOperation.hashSet(arguments.get(0), arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.HASH_HSETNX:
                if (argNum != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                flag = hashOperation.hashSetIfNotExists(arguments.get(0), arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            /**
             * todo
             */
            case ProtocolConstant.HASH_HMSET:
                flag = hashOperation.hashMultiSet(arguments.get(0), arguments);
                return SimpleResponse.ok();

            /**
             * todo
             */
            case ProtocolConstant.HASH_HMGET:
                list = hashOperation.hashMultiGet(arguments.get(0), arguments);
                return ListResponse.stringListResponse(list);

            default:
                break;
        }
        return ErrorResponse.errorCommon();
    }
}
