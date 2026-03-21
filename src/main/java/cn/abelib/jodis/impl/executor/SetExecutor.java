package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import cn.abelib.jodis.impl.operation.SetOperation;
import cn.abelib.jodis.protocol.*;
import cn.abelib.jodis.utils.StringUtils;
import cn.abelib.jodis.impl.executor.strategy.CommandStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:34
 */
public class SetExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();

    public SetExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();
    }
    
    /**
     * 注册所有命令策略
     */
    private void registerStrategies() {
        strategies.put(ProtocolConstant.SET_SADD, new SAddStrategy());
        strategies.put(ProtocolConstant.SET_SCARD, new SCardStrategy());
        strategies.put(ProtocolConstant.SET_SDIFF, new SDiffStrategy());
        strategies.put(ProtocolConstant.SET_SINTER, new SInterStrategy());
        strategies.put(ProtocolConstant.SET_SUNION, new SUnionStrategy());
        strategies.put(ProtocolConstant.SET_SISMEMBER, new SIsMemberStrategy());
        strategies.put(ProtocolConstant.SET_SMEMBERS, new SMembersStrategy());
        strategies.put(ProtocolConstant.SET_SMOVE, new SMoveStrategy());
        strategies.put(ProtocolConstant.SET_SPOP, new SPopStrategy());
        strategies.put(ProtocolConstant.SET_SRANDMEMBER, new SRandMemberStrategy());
        strategies.put(ProtocolConstant.SET_SREM, new SRemStrategy());
        strategies.put(ProtocolConstant.SET_SSCAN, new SScanStrategy());
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
        SetOperation setOperation = new SetOperation(jodisDb);
        String type = setOperation.type(key);
        if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, KeyType.JODIS_SET)) {
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
     * SADD key member
     */
    private class SAddStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            int num = op.setAdd(args.get(0), args.get(1));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * SCARD key
     */
    private class SCardStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            int num = op.setCard(args.get(0));
            return NumericResponse.numericResponse(num);
        }
    }
    
    /**
     * SDIFF key1 key2
     */
    private class SDiffStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            List<String> list = op.setDiff(args.get(0), args.get(1));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * SINTER key1 key2
     */
    private class SInterStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            List<String> list = op.setInter(args.get(0), args.get(1));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * SUNION key1 key2
     */
    private class SUnionStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            List<String> list = op.setUnion(args.get(0), args.get(1));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * SISMEMBER key member
     */
    private class SIsMemberStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            boolean flag = op.setIsMember(args.get(0), args.get(1));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * SMEMBERS key
     */
    private class SMembersStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            List<String> list = op.setMembers(args.get(0));
            return ListResponse.stringListResponse(list);
        }
    }
    
    /**
     * SMOVE source destination member
     */
    private class SMoveStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            boolean flag = op.setMove(args.get(0), args.get(1), args.get(2));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * SPOP key
     */
    private class SPopStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            String res = op.setPop(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * SRANDMEMBER key
     */
    private class SRandMemberStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            String res = op.setRandMember(args.get(0));
            return SimpleResponse.simpleResponse(res);
        }
    }
    
    /**
     * SREM key member
     */
    private class SRemStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            SetOperation op = new SetOperation(db);
            boolean flag = op.setRemove(args.get(0), args.get(1));
            return NumericResponse.numericResponse(flag ? 1 : 0);
        }
    }
    
    /**
     * SSCAN key cursor [MATCH pattern] [COUNT count]
     */
    private class SScanStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            // 参数解析：SSCAN key cursor [MATCH pattern] [COUNT count]
            String key = args.get(0);
            String cursor = args.get(1);
            
            // 默认值
            String pattern = null;
            int count = 10; // Redis 默认 COUNT 为 10
            
            // 解析可选参数 MATCH 和 COUNT
            for (int i = 2; i < args.size(); i++) {
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
            
            // 执行 SSCAN 操作
            SetOperation op = new SetOperation(db);
            List<String> result = op.setScan(key, cursor, pattern, count);
            
            // 返回数组响应
            return ListResponse.stringListResponse(result);
        }
    }
}