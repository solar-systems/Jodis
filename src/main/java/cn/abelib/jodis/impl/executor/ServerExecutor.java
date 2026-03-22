package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.ServerOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: abel.huang
 * @Date: 2020-08-13 00:40
 */
public class ServerExecutor implements Executor{
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public ServerExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.SERVER_PING, new PingStrategy());
        strategies.put(ProtocolConstant.SERVER_DBSIZE, new DbSizeStrategy());
        strategies.put(ProtocolConstant.SERVER_FLUSHDB, new FlushDbStrategy());
        strategies.put(ProtocolConstant.SERVER_BGREWRITEAOF, new BgRewriteAofStrategy());
    }
    
    /**
     * 执行命令
     */
    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argSize = arguments.size();
        
        // 1. 基础参数验证
        if (argSize != 0 && !command.equals(ProtocolConstant.SERVER_FLUSHDB)) {
            return ErrorResponse.errorArgsNum(command, 0, argSize);
        }
        
        // 2. 查找并执行策略
        CommandStrategy strategy = strategies.get(command);
        if (strategy == null) {
            return ErrorResponse.errorUnknownCmd(command);
        }
        
        return strategy.execute(jodisDb, arguments);
    }
    
    // ==================== 内部策略类 ====================
    
    /**
     * PING
     */
    private class PingStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ServerOperation op = new ServerOperation(db);
            String res = op.ping();
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * DBSIZE
     */
    private class DbSizeStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ServerOperation op = new ServerOperation(db);
            int num = op.dbSize();
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * FLUSHDB
     */
    private class FlushDbStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ServerOperation op = new ServerOperation(db);
            op.flushDb();
            return SimpleResponse.ok();
        }
    }
    
    /**
     * BGREWRITEAOF - 触发 WAL 重写
     */
    private class BgRewriteAofStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            try {
                db.rewriteWal();
                return SimpleResponse.simpleResponse("+Background append only file rewriting started");
            } catch (Exception e) {
                return ErrorResponse.error(e.getMessage());
            }
        }
    }
}
