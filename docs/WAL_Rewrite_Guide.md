# WAL Rewrite 调用时机说明

## 概述
`rewriteWal()` 方法用于重写 WAL（Write-Ahead Log）文件，将当前内存中的数据快照和重写期间的新请求写入新的 WAL 文件，然后原子替换旧文件。这可以压缩 WAL 文件大小，合并历史操作，加快重启恢复速度。

## 调用时机

### 1. 自动触发（推荐）
当 WAL 文件大小超过配置的阈值时，系统会自动触发重写。

**配置参数**：
```properties
# conf/jodis.properties
log.wal.rewrite.size=64 * 1024 * 1024  # 默认 64MB
```

**触发逻辑**：
- 每次执行写操作并写入 WAL 后，系统会检查 WAL 文件大小
- 如果文件大小 >= `log.wal.rewrite.size`，则异步触发重写
- 异步执行避免阻塞主线程，不影响正常请求处理

**代码位置**：
```java
// JodisDb.java - execute 方法中
public Response execute(Request request) throws IOException {
    Response response = executorFactory.execute(request);
    if (!noNeed && request.needLog() && !response.isError()) {
        if (rewriteWal.get()) {
            requestQueue.add(request);
        } else {
            walWriter.write(request.getRequest());
            checkAndTriggerRewrite();  // 检查并触发重写
        }
    }
    return response;
}
```

### 2. 手动触发 - BGREWRITEAOF 命令
通过客户端命令手动触发 WAL 重写，提供运维灵活性。

**命令格式**：
```bash
BGREWRITEAOF
```

**RESP 协议格式**：
```
*1\r\n$12\r\nBGREWRITEAOF\r\n
```

**响应**：
```
+Background append only file rewriting started
```

**使用示例**：
```bash
# 通过客户端发送命令
echo -e "*1\r\n\$12\r\nBGREWRITEAOF\r\n" | nc localhost 6059
```

**代码实现**：
```java
// ServerExecutor.java
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
```

### 3. 程序化调用
在代码中直接调用 `JodisDb.rewriteWal()` 方法。

**示例**：
```java
JodisDb jodisDb = new JodisDb(config);

// ... 执行业务逻辑后 ...

// 手动触发 WAL 重写
jodisDb.rewriteWal();
```

## 重写流程

1. **复制内存数据**：深拷贝当前内存中的所有数据
2. **设置重写标志**：`rewriteWal.set(true)`
3. **创建新文件**：创建 `.rewrite` 临时文件
4. **写入快照**：将内存数据转换为 SET/HSET/SADD 等命令写入新文件
5. **追加新请求**：将重写期间累积的新请求追加到新文件
6. **原子替换**：使用 `Files.move()` 原子替换原 WAL 文件
7. **清理状态**：清空请求队列，重置重写标志

## 并发控制

- 使用 `AtomicBoolean rewriteWal` 标志位控制并发
- 重写期间的新请求会被放入 `requestQueue` 队列
- 重写完成后，队列中的请求会被追加到新 WAL 文件
- 异步执行避免阻塞主线程

## 最佳实践

### 1. 生产环境配置
```properties
# 建议设置为预期 WAL 大小的 2-3 倍
log.wal.rewrite.size=64 * 1024 * 1024

# 启用 MIX 模式，结合 JDB 快照和 WAL
log.reload.mode=2
```

### 2. 监控 WAL 大小
定期检查 WAL 文件大小，确保磁盘空间充足：
```bash
ls -lh log/default.wal
```

### 3. 低峰期手动触发
对于重要业务，可以在低峰期手动执行 `BGREWRITEAOF` 命令，避免自动重写影响性能。

### 4. 备份策略
定期备份 WAL 和 JDB 文件：
```bash
cp log/default.wal backup/default.wal.$(date +%Y%m%d)
cp log/default.jdb backup/default.jdb.$(date +%Y%m%d)
```

## 注意事项

⚠️ **重要提示**：
1. WAL 重写是异步操作，不会阻塞正常请求处理
2. 重写过程中，新请求会被缓存到队列中
3. 不要手动编辑 WAL 文件，可能导致数据损坏
4. 确保日志目录有足够的磁盘空间（至少是 WAL 大小的 2 倍）

## 测试验证

### 单元测试
```java
@Test
public void testBgRewriteAofCommand() throws Exception {
    // 执行写操作
    String setCmd = "*3\r\n$3\r\nSET\r\n$4\r\ntest\r\n$5\r\nvalue\r\n";
    jodisDb.execute(setCmd);
    
    // 触发 WAL 重写
    String bgRewriteCmd = "*1\r\n$12\r\nBGREWRITEAOF\r\n";
    jodisDb.execute(bgRewriteCmd);
    
    // 验证数据存在
    Assert.assertTrue(jodisDb.containsKey("test"));
}
```

### 集成测试
运行完整的持久化测试：
```bash
mvn test -Dtest=PersistenceIntegrationTest
```

## 相关文件

- `src/main/java/cn/abelib/jodis/impl/JodisDb.java` - 核心实现
- `src/main/java/cn/abelib/jodis/impl/executor/ServerExecutor.java` - BGREWRITEAOF 命令处理
- `src/main/java/cn/abelib/jodis/protocol/ProtocolConstant.java` - 协议常量定义
- `src/test/java/cn/abelib/jodis/store/PersistenceIntegrationTest.java` - 测试用例
