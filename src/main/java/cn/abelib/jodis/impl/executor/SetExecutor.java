package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.SetOperation;
import cn.abelib.jodis.protocol.*;

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
        int argNum = arguments.size();
        int num;
        boolean flag;
        String res;
        List<String> list;
        switch (command) {
            case ProtocolConstant.SET_SADD:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                num = setOperation.setAdd(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.SET_SCARD:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                num = setOperation.setCard(arguments.get(0));
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.SET_SDIFF:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                list = setOperation.setDiff(arguments.get(0), arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SINTER:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                list = setOperation.setInter(arguments.get(0), arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SUNION:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                list = setOperation.setUnion(arguments.get(0), arguments.get(1));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SISMEMBER:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                flag = setOperation.setIsMember(arguments.get(0), arguments.get(1));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.SET_SMEMBERS:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                list = setOperation.setMembers(arguments.get(0));
                return ListResponse.stringListResponse(list);

            case ProtocolConstant.SET_SMOVE:
                if (arguments.size() != 3) {
                    return ErrorResponse.errorArgsNum(command, 3, argNum);
                }
                flag = setOperation.setMove(arguments.get(0), arguments.get(1), arguments.get(2));
                return NumericResponse.numericResponse(flag ? 1 : 0);

            case ProtocolConstant.SET_SPOP:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                res = setOperation.setPop(arguments.get(0));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.SET_SRANDMEMBER:
                if (arguments.size() != 1) {
                    return ErrorResponse.errorArgsNum(command, 1, argNum);
                }
                res = setOperation.setRandMember(arguments.get(0));
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.SET_SREM:
                if (arguments.size() != 2) {
                    return ErrorResponse.errorArgsNum(command, 2, argNum);
                }
                flag = setOperation.setRemove(arguments.get(0), arguments.get(1));
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
