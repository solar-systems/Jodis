package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.KeyOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.NumberUtils;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: abel.huang
 * @Date: 2020-08-08 14:09
 */
public class KeyExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public KeyExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.KEY_DEL, new DelStrategy());
        strategies.put(ProtocolConstant.KEY_TYPE, new TypeStrategy());
        strategies.put(ProtocolConstant.KEY_EXISTS, new ExistsStrategy());
        strategies.put(ProtocolConstant.KEY_KEYS, new KeysStrategy());
        strategies.put(ProtocolConstant.KEY_RANDOM_KEY, new RandomKeyStrategy());
        strategies.put(ProtocolConstant.KEY_RENAME, new RenameStrategy());
        strategies.put(ProtocolConstant.KEY_RENAMENX, new RenameNxStrategy());
        strategies.put(ProtocolConstant.KEY_EXPIRE, new ExpireStrategy());
        strategies.put(ProtocolConstant.KEY_EXPIRE_AT, new ExpireAtStrategy());
        strategies.put(ProtocolConstant.KEY_TTL, new TtlStrategy());
        strategies.put(ProtocolConstant.KEY_SCAN, new ScanStrategy());
    }
    
    /**
     * 执行命令
     */
    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        int argNum = arguments.size();
        
        // 1. 基础参数验证
        if (argNum < 1 && !command.equals(ProtocolConstant.KEY_RANDOM_KEY)) {
            return ErrorResponse.errorArgsNum(command);
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
     * DEL key
     */
    private class DelStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            int ans = op.delete(args.get(0));
            return NumericResponse.numericResponse(ans);
        }
    }
    
    /**
     * TYPE key
     */
    private class TypeStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            String res = op.type(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * EXISTS key
     */
    private class ExistsStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            boolean flag = op.exists(args.get(0));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * KEYS pattern
     */
    private class KeysStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            List<String> list = op.keys(args.get(0));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * RANDOMKEY
     */
    private class RandomKeyStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            String res = op.randomKey();
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * RENAME key newkey
     */
    private class RenameStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            boolean flag = op.rename(args.get(0), args.get(1));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * RENAMENX key newkey
     */
    private class RenameNxStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            boolean flag = op.renameIfNotExist(args.get(0), args.get(1));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * EXPIRE key seconds
     */
    private class ExpireStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            // 解析秒数
            Integer seconds = NumberUtils.parseInt(args.get(1));
            if (seconds == null || seconds < 0) {
                return ErrorResponse.errorInvalidNumber();
            }
            KeyOperation op = new KeyOperation(db);
            int ans = op.expire(args.get(0), seconds, TimeUnit.SECONDS);
            return NumericResponse.numericResponse(ans);
        }
    }
    
    /**
     * EXPIREAT key timestamp
     */
    private class ExpireAtStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            // 解析 Unix 时间戳（秒）
            Integer timestamp = NumberUtils.parseInt(args.get(1));
            if (timestamp == null || timestamp < 0) {
                return ErrorResponse.errorInvalidNumber();
            }
            KeyOperation op = new KeyOperation(db);
            int ans = op.expireAt(args.get(0), timestamp, TimeUnit.SECONDS);
            return NumericResponse.numericResponse(ans);
        }
    }
    
    /**
     * TTL key
     */
    private class TtlStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            KeyOperation op = new KeyOperation(db);
            int ans = op.ttl(args.get(0));
            return NumericResponse.numericResponse(ans);
        }
    }
    
    /**
     * SCAN cursor [MATCH pattern] [COUNT count]
     */
    private class ScanStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            // 参数解析：SCAN cursor [MATCH pattern] [COUNT count]
            String cursor = args.get(0);
            
            // 默认值
            String pattern = null;
            int count = 10; // Redis 默认 COUNT 为 10
            
            // 解析可选参数 MATCH 和 COUNT
            for (int i = 1; i < args.size(); i++) {
                String arg = args.get(i).toUpperCase();
                if ("MATCH".equals(arg) && i + 1 < args.size()) {
                    pattern = args.get(i + 1);
                    i++; // 跳过 pattern 值
                } else if ("COUNT".equals(arg) && i + 1 < args.size()) {
                    try {
                        count = Integer.parseInt(args.get(i + 1));
                        i++; // 跳过 count 值
                    } catch (NumberFormatException e) {
                        return ErrorResponse.errorInvalidNumber();
                    }
                }
            }
            
            // 执行 SCAN 操作
            KeyOperation op = new KeyOperation(db);
            List<String> result = op.scan(cursor, pattern, count);
            
            // 返回数组响应
            return ListResponse.stringListResponse(result);
        }
    }
}
