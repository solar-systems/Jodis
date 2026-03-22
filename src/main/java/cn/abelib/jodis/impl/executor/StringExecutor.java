package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.StringOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * String 类型命令执行器
 * @Author: abel.huang
 * @Date: 2020-07-16 00:58
 */
public class StringExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public StringExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.STRING_SET, new SetStrategy());
        strategies.put(ProtocolConstant.STRING_GET, new GetStrategy());
        strategies.put(ProtocolConstant.STRING_GETSET, new GetSetStrategy());
        strategies.put(ProtocolConstant.STRING_STRLEN, new StrLenStrategy());
        strategies.put(ProtocolConstant.STRING_INCR, new IncrStrategy());
        strategies.put(ProtocolConstant.STRING_INCRBY, new IncrByStrategy());
        strategies.put(ProtocolConstant.STRING_INCRBYFLOAT, new IncrByFloatStrategy());
        strategies.put(ProtocolConstant.STRING_DECR, new DecrStrategy());
        strategies.put(ProtocolConstant.STRING_DECRBY, new DecrByStrategy());
        strategies.put(ProtocolConstant.STRING_APPEND, new AppendStrategy());
        strategies.put(ProtocolConstant.STRING_MGET, new MGetStrategy());
        strategies.put(ProtocolConstant.STRING_MSET, new MSetStrategy());
        strategies.put(ProtocolConstant.STRING_SETEX, new SetExStrategy());
        strategies.put(ProtocolConstant.STRING_SETNX, new SetNxStrategy());
        strategies.put(ProtocolConstant.STRING_GETRANGE, new GetRangeStrategy());
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
        StringOperation stringOperation = new StringOperation(jodisDb);
        String type = stringOperation.type(key);
        if (type != null && !StringUtils.equals(type, KeyType.JODIS_STRING)) {
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
     * SET key value
     */
    private class SetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            op.set(args.get(0), args.get(1));
            return SimpleResponse.ok();
        }
    }
    
    /**
     * GET key
     */
    private class GetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            String res = op.get(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * GETSET key value
     */
    private class GetSetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            String res = op.getAndSet(args.get(0), args.get(1));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * STRLEN key
     */
    private class StrLenStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            Integer num = op.strLen(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * INCR key
     */
    private class IncrStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            Integer num = op.increment(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * INCRBY key increment
     */
    private class IncrByStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer num = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(num)) {
                return ErrorResponse.errorInvalidNumber();
            }
            StringOperation op = new StringOperation(db);
            num = op.incrementBy(args.get(0), num);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * INCRBYFLOAT key increment
     */
    private class IncrByFloatStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Float numFloat = NumberUtils.parseFloat(args.get(1));
            if (Objects.isNull(numFloat)) {
                return ErrorResponse.errorInvalidNumber();
            }
            StringOperation op = new StringOperation(db);
            numFloat = op.incrementByFloat(args.get(0), numFloat);
            return NumericResponse.numericResponse(numFloat);
        }
    }
    
    /**
     * DECR key
     */
    private class DecrStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            Integer num = op.decrement(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * DECRBY key decrement
     */
    private class DecrByStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer num = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(num)) {
                return ErrorResponse.errorInvalidNumber();
            }
            StringOperation op = new StringOperation(db);
            num = op.decrementBy(args.get(0), num);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * APPEND key value
     */
    private class AppendStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            Integer num = op.append(args.get(0), args.get(1));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * MGET key [key ...]
     */
    private class MGetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            List<String> list = op.multiGet(args);
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * MSET key value [key value ...]
     */
    private class MSetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            op.multiSet(args);
            return SimpleResponse.ok();
        }
    }
    
    /**
     * SETEX key seconds value
     */
    private class SetExStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer timeout = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(timeout)) {
                return ErrorResponse.errorInvalidNumber();
            }
            StringOperation op = new StringOperation(db);
            op.setExpire(args.get(0), timeout, args.get(2));
            return SimpleResponse.ok();
        }
    }
    
    /**
     * SETNX key value
     */
    private class SetNxStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            StringOperation op = new StringOperation(db);
            op.setIfNotExists(args.get(0), args.get(1));
            return SimpleResponse.ok();
        }
    }
    
    /**
     * GETRANGE key start end
     */
    private class GetRangeStrategy implements CommandStrategy {
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
            StringOperation op = new StringOperation(db);
            String res = op.getRange(args.get(0), start, end);
            return SimpleResponse.simpleResponse(res);
        }
    }
}
