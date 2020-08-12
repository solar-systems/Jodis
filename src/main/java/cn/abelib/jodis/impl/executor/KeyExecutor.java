package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.KeyOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.NumberUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: abel.huang
 * @Date: 2020-08-08 14:09
 */
public class KeyExecutor implements Executor {
    private KeyOperation keyOperation;

    public KeyExecutor(JodisDb jodisDb) {
        this.keyOperation = new KeyOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argNum = arguments.size();
        boolean flag;
        int ans;
        long timestamp;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.KEY_DEL:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                ans = keyOperation.delete(arguments.get(0));
                return NumericResponse.numericResponse(ans);

            case ProtocolConstant.KEY_TYPE:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                res = keyOperation.type(arguments.get(0));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.KEY_EXISTS:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                flag = keyOperation.exists(arguments.get(0));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.KEY_KEYS:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                list = keyOperation.keys(arguments.get(0));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.KEY_RANDOM_KEY:
                if (arguments.size() != 0) {
                    return ErrorResponse.errorArgsNum(command, 0, argNum);
                }
                res = keyOperation.randomKey();
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.KEY_RENAME:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                flag = keyOperation.rename(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.KEY_RENAMENX:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                flag = keyOperation.renameIfNotExist(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.KEY_EXPIRE:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                timestamp = NumberUtils.toTimestamp(arguments.get(1));
                if (timestamp < 0)  {
                    ans = 0;
                } else {
                    ans = keyOperation.expire(arguments.get(0), timestamp, TimeUnit.SECONDS);
                }
                return NumericResponse.numericResponse(ans);

            case ProtocolConstant.KEY_EXPIRE_AT:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                timestamp = NumberUtils.toTimestamp(arguments.get(1));
                if (timestamp < 0)  {
                    ans = 0;
                } else {
                    ans = keyOperation.expireAt(arguments.get(0), timestamp, TimeUnit.SECONDS);
                }
                return NumericResponse.numericResponse(ans);

            case ProtocolConstant.KEY_TTL:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                timestamp = NumberUtils.toTimestamp(arguments.get(1));
                if (timestamp < 0)  {
                    ans = 0;
                } else {
                    ans = keyOperation.ttl(arguments.get(0), timestamp, TimeUnit.SECONDS);
                }
                return NumericResponse.numericResponse(ans);

            default:
                break;

        }
        return ErrorResponse.errorCommon();
    }
}
