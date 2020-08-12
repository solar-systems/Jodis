package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.SetOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;

import java.util.List;


/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class SetExecutor implements Executor {
    private SetOperation setOperation;

    public SetExecutor(JodisDb jodisDb) {
        this.setOperation = new SetOperation(jodisDb);
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
        String type = setOperation.type(key);
        // 类型不匹配
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_SET)) {
            return ErrorResponse.errorSyntax();
        }
        int num;
        boolean flag;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.SET_SADD:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                num = setOperation.setAdd(key, arguments.get(1));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.SET_SCARD:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                num = setOperation.setCard(key);
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.SET_SDIFF:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                list = setOperation.setDiff(key, arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SINTER:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                list = setOperation.setInter(key, arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SUNION:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                list = setOperation.setUnion(key, arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SISMEMBER:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                flag = setOperation.setIsMember(key, arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.SET_SMEMBERS:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                list = setOperation.setMembers(key);
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SMOVE:
                if (arguments.size() != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argSize);
                }
                flag = setOperation.setMove(key, arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.SET_SPOP:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                res = setOperation.setPop(key);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.SET_SRANDMEMBER:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argSize);
                }
                res = setOperation.setRandMember(key);
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.SET_SREM:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argSize);
                }
                flag = setOperation.setRemove(key, arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            /**
             * todo
             */
            case ProtocolConstant.SET_SSCAN:
                return SimpleResponse.ok();

            default:
                break;
        }
        return ErrorResponse.errorCommon();
    }
}
