package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.ServerOperation;
import cn.abelib.jodis.protocol.*;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-08-13 00:40
 */
public class ServerExecutor implements Executor{
    private ServerOperation serverOperation;

    public ServerExecutor(JodisDb jodisDb) {
        this.serverOperation = new ServerOperation(jodisDb);
    }

    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argSize = arguments.size();
        int num;
        String res;
        switch (command) {
            case ProtocolConstant.SERVER_PING:
                if (argSize != 0) {
                    return ErrorResponse.errorArgsNum(command, 0, argSize);
                }
                res = serverOperation.ping();
                return SimpleResponse.simpleResponse(res);

            case ProtocolConstant.SERVER_DBSIZE:
                if (argSize != 0) {
                    return ErrorResponse.errorArgsNum(command, 0, argSize);
                }
                num = serverOperation.dbSize();
                return NumericResponse.numericResponse(num);

            case ProtocolConstant.SERVER_FLUSHDB:
                if (argSize != 0) {
                    return ErrorResponse.errorArgsNum(command, 0, argSize);
                }
                serverOperation.flushDb();
                return SimpleResponse.ok();
            default:

        }

        return null;
    }
}
