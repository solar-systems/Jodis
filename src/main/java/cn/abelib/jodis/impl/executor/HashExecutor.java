package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.HashOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;

import java.util.List;
import java.util.Objects;

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
        int argSize = arguments.size();
        if (argSize < 1) {
            return ErrorResponse.errorArgsNum(command);
        }
        String key = arguments.get(0);
        if (StringUtils.isEmpty(key)) {
            return ErrorResponse.errorArgsNum(command);
        }
        String type = hashOperation.type(key);
        // 类型不匹配
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_HASH)) {
            return ErrorResponse.errorSyntax();
        }
        Integer num;
        Float numFloat;
        boolean flag;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.HASH_HDEL:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = hashOperation.hashDelete(key, arguments.get(1));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HEXISTS:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                flag = hashOperation.hashExists(key, arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.HASH_HGET:
                if (argSize != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                res = hashOperation.hashGet(key, arguments.get(1));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.HASH_HGETALL:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                list = hashOperation.hashGetAll(key);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HINCRBY:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                num = NumberUtils.parseInt(arguments.get(2));
                if (Objects.isNull(num)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                num = hashOperation.hashIncrementBy(key, arguments.get(1), num);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HINCRBYFLOAT:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                numFloat = NumberUtils.parseFloat(arguments.get(2));
                if (Objects.isNull(numFloat)) {
                    return ErrorResponse.errorInvalidNumber();
                }
                numFloat = hashOperation.hashIncrementByFloat(key, arguments.get(1), numFloat);
                return NumericResponse.numericResponse(numFloat);

            case ProtocolConstant.HASH_HKEYS:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                list = hashOperation.hashKeys(key);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HVALS:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                list = hashOperation.hashValues(key);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.HASH_HLEN:
                if (argSize != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = hashOperation.hashLen(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.HASH_HSET:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                flag = hashOperation.hashSet(key, arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.HASH_HSETNX:
                if (argSize != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                flag = hashOperation.hashSetIfNotExists(key, arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.HASH_HMSET:
                if (argSize % 2 == 0) {
                    return ErrorResponse.errorArgsNum(command);
                }
                list = arguments.subList(1, argSize);
                hashOperation.hashMultiSet(key, list);
                return SimpleResponse.ok();

            case ProtocolConstant.HASH_HMGET:
                if (argSize < 2) {
                    return ErrorResponse.errorArgsNum(command);
                }
                list = arguments.subList(1, argSize);
                list = hashOperation.hashMultiGet(key, list);
                return ListResponse.stringListResponse(list);

            default:
                break;
        }
        return ErrorResponse.errorCommon();
    }
}
