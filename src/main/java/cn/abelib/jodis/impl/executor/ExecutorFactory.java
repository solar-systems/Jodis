package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ErrorResponse;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:36
 */
public class ExecutorFactory {
    private Executor keyExecutor;
    private Executor stringExecutor;
    private Executor listExecutor;
    private Executor setExecutor;
    private Executor hashExecutor;
    private Executor sortedSetExecutor;

    public ExecutorFactory(JodisDb jodisDb) {
        this.keyExecutor = new KeyExecutor(jodisDb);
        this.stringExecutor = new StringExecutor(jodisDb);
        this.listExecutor = new ListExecutor(jodisDb);
        this.setExecutor = new SetExecutor(jodisDb);
        this.hashExecutor = new HashExecutor(jodisDb);
        this.sortedSetExecutor = new SortedSetExecutor(jodisDb);
    }

    /**
     * todo 需要先判断一下类型是否匹配对应的命令
     * @param request
     * @return
     */
    public Response execute(Request request) {
        String command = request.getCommand();
        // 判断解析请求中是否存在错误
        if (request.isError()) {
            return request.errorResponse();
        }
        Response response;
        if (ProtocolConstant.KEY_CMDS.contains(command)) {
            response = this.keyExecutor.execute(request);
        } else if (ProtocolConstant.STRING_CMDS.contains(command)) {
            response = this.stringExecutor.execute(request);
        } else if (ProtocolConstant.LIST_CMDS.contains(command)) {
            response = this.listExecutor.execute(request);
        } else if (ProtocolConstant.HASH_CMDS.contains(command)) {
            response = this.hashExecutor.execute(request);
        } else if (ProtocolConstant.SET_CMDS.contains(command)) {
            response = this.setExecutor.execute(request);
        } else if (ProtocolConstant.ZSET_CMDS.contains(command)) {
            response = this.sortedSetExecutor.execute(request);
        } else {
            // cmd is unknown
            response = ErrorResponse.errorUnknownCmd(command);
        }
        return response;
    }
}
