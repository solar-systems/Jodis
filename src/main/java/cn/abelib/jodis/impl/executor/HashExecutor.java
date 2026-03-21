package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.HashOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.utils.NumberUtils;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:35
 */
public class HashExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public HashExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.HASH_HDEL, new HDelStrategy());
        strategies.put(ProtocolConstant.HASH_HEXISTS, new HExistsStrategy());
        strategies.put(ProtocolConstant.HASH_HGET, new HGetStrategy());
        strategies.put(ProtocolConstant.HASH_HGETALL, new HGetAllStrategy());
        strategies.put(ProtocolConstant.HASH_HINCRBY, new HIncrByStrategy());
        strategies.put(ProtocolConstant.HASH_HINCRBYFLOAT, new HIncrByFloatStrategy());
        strategies.put(ProtocolConstant.HASH_HKEYS, new HKeysStrategy());
        strategies.put(ProtocolConstant.HASH_HVALS, new HValsStrategy());
        strategies.put(ProtocolConstant.HASH_HLEN, new HLenStrategy());
        strategies.put(ProtocolConstant.HASH_HSET, new HSetStrategy());
        strategies.put(ProtocolConstant.HASH_HSETNX, new HSetNxStrategy());
        strategies.put(ProtocolConstant.HASH_HMSET, new HMSetStrategy());
        strategies.put(ProtocolConstant.HASH_HMGET, new HMGetStrategy());
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
        HashOperation hashOperation = new HashOperation(jodisDb);
        String type = hashOperation.type(key);
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_HASH)) {
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
     * HDEL key field
     */
    private class HDelStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            Integer num = op.hashDelete(args.get(0), args.get(1));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * HEXISTS key field
     */
    private class HExistsStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            boolean flag = op.hashExists(args.get(0), args.get(1));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * HGET key field
     */
    private class HGetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            String res = op.hashGet(args.get(0), args.get(1));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * HGETALL key
     */
    private class HGetAllStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            List<String> list = op.hashGetAll(args.get(0));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * HINCRBY key field increment
     */
    private class HIncrByStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Integer num = NumberUtils.parseInt(args.get(2));
            if (Objects.isNull(num)) {
                return ErrorResponse.errorInvalidNumber();
            }
            HashOperation op = new HashOperation(db);
            num = op.hashIncrementBy(args.get(0), args.get(1), num);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * HINCRBYFLOAT key field increment
     */
    private class HIncrByFloatStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Float numFloat = NumberUtils.parseFloat(args.get(2));
            if (Objects.isNull(numFloat)) {
                return ErrorResponse.errorInvalidNumber();
            }
            HashOperation op = new HashOperation(db);
            numFloat = op.hashIncrementByFloat(args.get(0), args.get(1), numFloat);
            return NumericResponse.numericResponse(numFloat);
        }
    }
    
    /**
     * HKEYS key
     */
    private class HKeysStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            List<String> list = op.hashKeys(args.get(0));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * HVALS key
     */
    private class HValsStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            List<String> list = op.hashValues(args.get(0));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * HLEN key
     */
    private class HLenStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            Integer num = op.hashLen(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * HSET key field value
     */
    private class HSetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            boolean flag = op.hashSet(args.get(0), args.get(1), args.get(2));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * HSETNX key field value
     */
    private class HSetNxStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            boolean flag = op.hashSetIfNotExists(args.get(0), args.get(1), args.get(2));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * HMSET key field value [field value ...]
     */
    private class HMSetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            List<String> list = args.subList(1, args.size());
            op.hashMultiSet(args.get(0), list);
            return SimpleResponse.ok();
        }
    }
    
    /**
     * HMGET key field [field ...]
     */
    private class HMGetStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            HashOperation op = new HashOperation(db);
            List<String> list = args.subList(1, args.size());
            list = op.hashMultiGet(args.get(0), list);
            return ListResponse.stringListResponse(list);
        }
    }
}