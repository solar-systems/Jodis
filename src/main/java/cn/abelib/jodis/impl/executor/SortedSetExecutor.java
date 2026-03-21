package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.SortedSetOperation;
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
 * @Date: 2020-07-16 01:34
 */
public class SortedSetExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public SortedSetExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.ZSET_ZADD, new ZAddStrategy());
        strategies.put(ProtocolConstant.ZSET_ZCARD, new ZCardStrategy());
        strategies.put(ProtocolConstant.ZSET_ZSCORE, new ZScoreStrategy());
        strategies.put(ProtocolConstant.ZSET_ZCOUNT, new ZCountStrategy());
        strategies.put(ProtocolConstant.ZSET_ZREM, new ZRemStrategy());
        strategies.put(ProtocolConstant.ZSET_ZRANGE, new ZRangeStrategy());
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
        SortedSetOperation sortedSetOperation = new SortedSetOperation(jodisDb);
        String type = sortedSetOperation.type(key);
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_ZSET)) {
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
     * ZADD key score member
     */
    private class ZAddStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Double score = NumberUtils.parseDouble(args.get(1));
            if (Objects.isNull(score)) {
                return ErrorResponse.errorInvalidNumber();
            }
            SortedSetOperation op = new SortedSetOperation(db);
            int num = op.zAdd(args.get(0), score, args.get(2));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * ZCARD key
     */
    private class ZCardStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SortedSetOperation op = new SortedSetOperation(db);
            int num = op.zCard(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * ZSCORE key member
     */
    private class ZScoreStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SortedSetOperation op = new SortedSetOperation(db);
            Double score = op.zScore(args.get(0), args.get(1));
            return NumericResponse.numericResponse(score);
        }
    }
    
    /**
     * ZCOUNT key min max
     */
    private class ZCountStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Double min = NumberUtils.parseDouble(args.get(1));
            if (Objects.isNull(min)) {
                return ErrorResponse.errorInvalidNumber();
            }
            Double max = NumberUtils.parseDouble(args.get(2));
            if (Objects.isNull(max)) {
                return ErrorResponse.errorInvalidNumber();
            }
            SortedSetOperation op = new SortedSetOperation(db);
            int num = op.zCount(args.get(0), min, max);
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * ZREM key member
     */
    private class ZRemStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SortedSetOperation op = new SortedSetOperation(db);
            int num = op.zRemove(args.get(0), args.get(1));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * ZRANGE key start stop
     */
    private class ZRangeStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            Long start = NumberUtils.parseLong(args.get(1));
            if (Objects.isNull(start)) {
                return ErrorResponse.errorInvalidNumber();
            }
            Long end = NumberUtils.parseLong(args.get(2));
            if (Objects.isNull(end)) {
                return ErrorResponse.errorInvalidNumber();
            }
            SortedSetOperation op = new SortedSetOperation(db);
            List<String> members = op.zRange(args.get(0), start, end);
            return ListResponse.stringListResponse(members);
        }
    }
}
