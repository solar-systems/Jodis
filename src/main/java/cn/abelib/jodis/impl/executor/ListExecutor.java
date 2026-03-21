package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.ListOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class ListExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public ListExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.LIST_LINDEX, new LIndexStrategy());
        strategies.put(ProtocolConstant.LIST_LINSERT, new LInsertStrategy());
        strategies.put(ProtocolConstant.LIST_LPOP, new LPopStrategy());
        strategies.put(ProtocolConstant.LIST_RPOP, new RPopStrategy());
        strategies.put(ProtocolConstant.LIST_LPUSH, new LPushStrategy());
        strategies.put(ProtocolConstant.LIST_RPUSH, new RPushStrategy());
        strategies.put(ProtocolConstant.LIST_LRANGE, new LRangeStrategy());
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
        if (argSize < 1) {
            return ErrorResponse.errorArgsNum(command);
        }
        
        String key = arguments.get(0);
        if (StringUtils.isEmpty(key)) {
            return ErrorResponse.errorArgsNum(command);
        }
        
        // 2. 类型检查
        ListOperation listOperation = new ListOperation(jodisDb);
        String type = listOperation.type(key);
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_LIST)) {
            return ErrorResponse.errorSyntax();
        }
        
        // 3. 查找并执行策略
        CommandStrategy strategy = strategies.get(command);
        if (strategy == null) {
            return ErrorResponse.errorUnknownCmd(command);
        }
        
        return strategy.execute(jodisDb, arguments);
    }
    
    // ==================== 内部策略类 ====================
    
    /**
     * LINDEX key index
     */
    private class LIndexStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer index = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(index)) {
                return ErrorResponse.errorInvalidNumber();
            }
            ListOperation op = new ListOperation(db);
            String res = op.leftIndex(args.get(0), index);
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * LINSERT key BEFORE|AFTER pivot value
     */
    private class LInsertStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            String position = args.get(2);
            ListOperation op = new ListOperation(db);
            Integer num;
            if (StringUtils.equalsIgnoreCase(position, ProtocolConstant.LIST_BEFORE)) {
                num = op.leftInsert(args.get(0), args.get(1), args.get(3));
            } else if (StringUtils.equalsIgnoreCase(position, ProtocolConstant.LIST_AFTER)) {
                num = op.rightInsert(args.get(0), args.get(1), args.get(3));
            } else {
                return ErrorResponse.errorSyntax();
            }
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * LPOP key
     */
    private class LPopStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ListOperation op = new ListOperation(db);
            String res = op.leftPop(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * RPOP key
     */
    private class RPopStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ListOperation op = new ListOperation(db);
            String res = op.rightPop(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * LPUSH key value [value ...]
     */
    private class LPushStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ListOperation op = new ListOperation(db);
            List<String> values = new ArrayList<>(args.subList(1, args.size()));
            Integer num = op.leftPush(args.get(0), values);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * RPUSH key value [value ...]
     */
    private class RPushStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            ListOperation op = new ListOperation(db);
            List<String> values = new ArrayList<>(args.subList(1, args.size()));
            Integer num = op.rightPush(args.get(0), values);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * LRANGE key start stop
     */
    private class LRangeStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer start = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(start)) {
                return ErrorResponse.errorInvalidNumber();
            }
            Integer end = NumberUtils.parseInt(args.get(2));
            if (Objects.isNull(end)) {
                return ErrorResponse.errorInvalidNumber();
            }
            ListOperation op = new ListOperation(db);
            List<String> list = op.listRange(args.get(0), start, end);
            return ListResponse.stringListResponse(list);
        }
    }
}