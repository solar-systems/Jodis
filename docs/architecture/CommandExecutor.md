# 命令执行框架 - 策略模式实现

Jodis 使用策略模式实现命令执行框架，每个 Executor 负责一种数据类型的命令处理。

## 架构设计

### Executor 层次结构

```
Executor (接口)
    ├── StringExecutor        // String 类型命令
    ├── ListExecutor          // List 类型命令
    ├── HashExecutor          // Hash 类型命令
    ├── SetExecutor           // Set 类型命令
    ├── SortedSetExecutor     // SortedSet 类型命令
    ├── KeyExecutor           // Key 操作命令
    └── ServerExecutor        // Server 命令
```

### ExecutorFactory

```java
public class ExecutorFactory {
    private final JodisDb jodisDb;
    
    public ExecutorFactory(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }
    
    public Response execute(Request request) {
        String command = request.getCommand();
        
        // 根据命令类型选择对应的 Executor
        if (ProtocolConstant.STRING_CMDS.contains(command)) {
            return new StringExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.LIST_CMDS.contains(command)) {
            return new ListExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.HASH_CMDS.contains(command)) {
            return new HashExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.SET_CMDS.contains(command)) {
            return new SetExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.ZSET_CMDS.contains(command)) {
            return new SortedSetExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.KEY_CMDS.contains(command)) {
            return new KeyExecutor(jodisDb).execute(request);
        } else if (ProtocolConstant.SERVER_CMDS.contains(command)) {
            return new ServerExecutor(jodisDb).execute(request);
        } else {
            return ErrorResponse.errorUnknownCmd(command);
        }
    }
}
```

## CommandStrategy 接口

### 定义

```java
/**
 * 命令策略接口
 */
public interface CommandStrategy {
    /**
     * 执行具体命令逻辑
     * @param jodisDb 数据库实例
     * @param args 命令参数（不包含命令名）
     * @return 执行结果
     */
    Response execute(JodisDb jodisDb, List<String> args);
    
    /**
     * 获取必需参数数量（可选）
     * @return -1 表示不限制
     */
    default int getRequiredArgCount() {
        return -1;
    }
}
```

### 优势

1. **职责单一**：每个策略类只负责一个命令
2. **易于测试**：可以独立测试每个策略
3. **便于扩展**：新增命令只需添加新策略类
4. **类型安全**：编译期检查参数和方法

## StringExecutor 示例

### 完整实现

```java
public class StringExecutor implements Executor {
    private final JodisDb jodisDb;
    private final Map<String, CommandStrategy> strategies = new HashMap<>();
    
    public StringExecutor(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
        registerStrategies();  // 注册所有命令策略
    }
    
    private void registerStrategies() {
        strategies.put(ProtocolConstant.STRING_SET, new SetStrategy());
        strategies.put(ProtocolConstant.STRING_GET, new GetStrategy());
        strategies.put(ProtocolConstant.STRING_GETSET, new GetSetStrategy());
        strategies.put(ProtocolConstant.STRING_MGET, new MGetStrategy());
        strategies.put(ProtocolConstant.STRING_MSET, new MSetStrategy());
        strategies.put(ProtocolConstant.STRING_INCR, new IncrStrategy());
        strategies.put(ProtocolConstant.STRING_INCRBY, new IncrByStrategy());
        strategies.put(ProtocolConstant.STRING_DECR, new DecrStrategy());
        strategies.put(ProtocolConstant.STRING_DECRBY, new DecrByStrategy());
        strategies.put(ProtocolConstant.STRING_STRLEN, new StrLenStrategy());
        strategies.put(ProtocolConstant.STRING_SETEX, new SetExStrategy());
        strategies.put(ProtocolConstant.STRING_SETNX, new SetNxStrategy());
        strategies.put(ProtocolConstant.STRING_APPEND, new AppendStrategy());
        strategies.put(ProtocolConstant.STRING_GETRANGE, new GetRangeStrategy());
    }
    
    @Override
    public Response execute(Request request) {
        String command = request.getCommand();
        List<String> arguments = request.getArgs();
        
        // 1. 基础参数验证
        if (arguments == null || arguments.isEmpty()) {
            return ErrorResponse.errorArgsNum(command);
        }
        
        // 2. 查找并执行对应的策略
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
            if (args.size() < 2) {
                return ErrorResponse.errorArgsNum("SET");
            }
            
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
            if (args.size() < 1) {
                return ErrorResponse.errorArgsNum("GET");
            }
            
            StringOperation op = new StringOperation(db);
            String result = op.get(args.get(0));
            
            if (result == null) {
                return SimpleResponse.nil();  // Redis nil 响应
            }
            
            return SimpleResponse.simpleResponse(result);
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
     * SETEX key seconds value
     */
    private class SetExStrategy implements CommandStrategy {
        @Override
        public Response execute(JodisDb db, List<String> args) {
            if (args.size() < 3) {
                return ErrorResponse.errorArgsNum("SETEX");
            }
            
            Integer timeout = NumberUtils.parseInt(args.get(1));
            if (Objects.isNull(timeout)) {
                return ErrorResponse.errorInvalidNumber();
            }
            
            StringOperation op = new StringOperation(db);
            op.setExpire(args.get(0), timeout, args.get(2));
            return SimpleResponse.ok();
        }
    }
}
```

## Operation 层

### 作用

封装具体的业务逻辑，供 Executor 调用。

### StringOperation 示例

```java
public class StringOperation extends KeyOperation {
    
    public StringOperation(JodisDb jodisDb) {
        super(jodisDb);
    }
    
    /**
     * SET key value
     */
    public int set(String key, String value) {
        jodisDb.put(key, JodisObject.putJodisString(value));
        return value.length();
    }
    
    /**
     * GET key
     */
    public String get(String key) {
        if (!exists(key)) {
            return null;
        }
        return getJodisString(key).getHolder();
    }
    
    /**
     * INCR key
     */
    public int increment(String key) {
        return incrementBy(key, 1);
    }
    
    /**
     * INCRBY key increment
     */
    public int incrementBy(String key, int incrAmount) {
        if (!exists(key)) {
            set(key, String.valueOf(incrAmount));
            return incrAmount;
        }
        
        String value = get(key);
        int incr;
        try {
            incr = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;  // 返回值不是整数
        }
        
        incr += incrAmount;
        set(key, String.valueOf(incr));
        return incr;
    }
    
    /**
     * SETEX key seconds value
     */
    public boolean setExpire(String key, int timeout, String value) {
        set(key, value);
        expire(key, timeout, TimeUnit.SECONDS);
        return true;
    }
}
```

## 类型检查机制

### 自动类型验证

```java
// StringExecutor 中的类型检查
String type = stringOperation.type(key);
if (type != null && !StringUtils.equals(type, KeyType.JODIS_STRING)) {
    return ErrorResponse.errorSyntax();
}
```

**效果**：
- ✅ 防止对 Hash 类型的 key 执行 GET 命令
- ✅ 防止对 List 类型的 key 执行 HSET 命令
- ✅ 返回标准的 WRONGTYPE 错误

## 扩展性

### 新增命令步骤

1. **在 ProtocolConstant 中定义命令常量**
   ```java
   String STRING_NEWCMD = "NEWCMD";
   ```

2. **在 Executor 中注册策略**
   ```java
   strategies.put(ProtocolConstant.STRING_NEWCMD, new NewCmdStrategy());
   ```

3. **实现策略类**
   ```java
   private class NewCmdStrategy implements CommandStrategy {
       @Override
       public Response execute(JodisDb db, List<String> args) {
           // 业务逻辑
           StringOperation op = new StringOperation(db);
           op.newCmd(args.get(0), args.get(1));
           return SimpleResponse.ok();
       }
   }
   ```

4. **在 Operation 中添加方法**
   ```java
   public void newCmd(String key, String value) {
       // 具体实现
   }
   ```

## 性能优化

### 1. 减少对象创建

```java
// ❌ 每次请求都创建新对象
private static final Response OK = SimpleResponse.ok();

// ✅ 复用常量对象
public Response execute(...) {
    return SimpleResponse.OK;  // 静态常量
}
```

### 2. 快速失败

```java
// 参数验证前置
if (args.size() < requiredArgs) {
    return ErrorResponse.errorArgsNum(command);
}
```

### 3. 缓存热点数据

```java
// ConcurrentHashMap 天然支持并发访问
private final Map<String, CommandStrategy> strategies = new ConcurrentHashMap<>();
```

## 相关文件

- [系统架构](SystemArchitecture.md)
- [RESP 协议详解](RespProtocol.md)
- [内存存储结构](MemoryStorage.md)
