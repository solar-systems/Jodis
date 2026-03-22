# 数据持久化机制

Jodis 提供两种持久化方式：WAL（Write-Ahead Log）和 JDB（Jodis DataBase Snapshot），确保数据安全。

## WAL - 预写日志

### 原理

所有写操作先记录到日志文件，再执行内存更新。

```
客户端请求 → 追加 WAL → 更新内存 → 返回响应
```

### 写入流程

```java
public class WalWriter {
    private Path aofFile;
    private RandomAccessFile raFile;
    private FileChannel channel;
    
    public boolean write(String reqCmd) throws IOException {
        // 添加换行符
        reqCmd = reqCmd + StringUtils.CLRF;
        byte[] data = ByteUtils.getBytesUTF8(reqCmd);
        
        // 追加写入
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        
        // 强制刷盘（fsync）
        channel.force(true);
        
        return true;
    }
}
```

**关键点**：
- ✅ 每次写入后调用 `fsync()` 确保数据落盘
- ✅ 追加模式写入，性能优秀
- ✅ 每条记录以 `\r\n` 结尾，方便读取

### 读取流程

```java
public class WalReader {
    public Iterator<String> readWal() throws IOException {
        logger.info("Read WAL file started");
        List<String> lines = new ArrayList<>();
        Iterator<String> iterator = readFile(walFile, "WAL");
        
        while (iterator.hasNext()) {
            lines.add(iterator.next());
        }
        
        logger.info("WAL file contains {} entries", lines.size());
        return lines.iterator();
    }
}
```

**恢复流程**：
```java
private void loadFromWal() throws IOException {
    Iterator<String> iterator = walReader.readWal();
    while (iterator.hasNext()) {
        String request = iterator.next();
        if (StringUtils.isNotEmpty(request)) {
            execute(request.trim(), false);  // 重放命令
        }
    }
}
```

### WAL Rewrite

**目的**：避免 WAL 文件无限增长。

```java
public void rewriteWal() throws IOException {
    // 1. 复制当前内存数据
    Map<String, JodisObject> source = CollectionUtils.deepCopyMap(jodisCollection);
    
    // 2. 设置重写标志
    rewriteWal.set(true);
    walWriter.startRewrite();
    
    // 3. 将内存快照写入新文件
    for (Map.Entry<String, JodisObject> entry : source.entrySet()) {
        Request cmd = buildCommand(entry);
        walWriter.rewrite(cmd.toString());
    }
    
    // 4. 将重写期间的新请求追加到新文件
    for (Request req : requestQueue) {
        walWriter.rewrite(req.toString());
    }
    
    // 5. 原子替换原文件
    completeWalRewrite();
    
    // 6. 清空队列
    requestQueue.clear();
    rewriteWal.set(false);
}
```

**优势**：
- ✅ 压缩 WAL 文件大小
- ✅ 合并历史操作（如多次 SET 只保留最后一次）
- ✅ 加快重启恢复速度

## JDB - 数据快照

### 文件格式

```
┌──────────────┐
│ Magic Flag   │  "JODIS_JDB"
├──────────────┤
│ Version      │  "0001"
├──────────────┤
│ Entry 1      │  [Type][Len][Key][Value][ExpireTime]
├──────────────┤
│ Entry 2      │  ...
├──────────────┤
│ EOF Marker   │  0xFF
└──────────────┘
```

### 写入流程

```java
public class JdbWriter {
    private Path jdbFile;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    
    public void writeSnapshot(Map<String, JodisObject> data) throws IOException {
        writeFileHeader();  // 写入文件头
        
        for (Map.Entry<String, JodisObject> entry : data.entrySet()) {
            ByteBuffer buffer = writeEntry(entry.getKey(), entry.getValue());
            bos.write(buffer.array());
        }
        
        writeFileFooter();  // 写入 EOF 标记
        fos.getChannel().force(true);  // 强制刷盘
    }
    
    private ByteBuffer writeString(String key, JodisObject obj) {
        JodisString value = (JodisString) obj.getValue();
        long expireTime = obj.getExpireTime();  // TTL 信息
        
        int len = JDB_TYPE_SIZE + JDB_INT_SIZE + 
                  keysLen + valuesLen + JDB_LONG_SIZE;
        
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(JDB_TYPE_STRING);
        buffer.putInt(len);
        buffer.put(keys);      // Key
        buffer.put(values);    // Value
        buffer.putLong(expireTime);  // 过期时间
        
        return buffer;
    }
}
```

**支持的数据类型**：
- String
- List
- Hash
- Set
- SortedSet

### 读取流程

```java
public class JdbReader {
    public Map<String, JodisObject> readSnapshot() throws IOException {
        Map<String, JodisObject> data = new HashMap<>();
        long now = System.currentTimeMillis();
        
        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(jdbFile.toFile()))) {
            
            // 1. 验证文件头
            verifyFileHeader(dis);
            
            // 2. 读取所有 Entry
            while (true) {
                byte type = dis.readByte();
                if (type == EOF) break;
                
                int totalLen = dis.readInt();
                byte[] entryData = new byte[totalLen - 1];
                dis.readFully(entryData);
                
                ByteBuffer buffer = ByteBuffer.wrap(entryData);
                KeyValue<String, JodisObject> entry = parseEntry(type, buffer);
                
                // 3. 检查是否过期
                if (entry.getValue().getExpireTime() > 0 && 
                    entry.getValue().getExpireTime() < now) {
                    continue;  // 跳过过期数据
                }
                
                data.put(entry.getKey(), entry.getValue());
            }
            
            return data;
        }
    }
}
```

## 双重持久化策略

### 配置选项

```properties
# conf/jodis.properties

# 加载模式：0=WAL, 1=JDB, 2=MIX
log.reload.mode=2

# WAL 文件
log.wal=default.wal

# JDB 文件
log.jdb=default.jdb

# WAL 重写阈值（字节）
log.wal.rewrite.size=67108864  # 64MB
```

### MIX 模式（推荐）

**启动流程**：
```
1. 加载 JDB 快照（快速恢复大部分数据）
   ↓
2. 重放 WAL 日志（恢复最近的操作）
   ↓
3. 数据恢复完成
```

**优势**：
- ✅ 结合两种方式的优点
- ✅ 启动速度快（JDB 全量 + WAL 增量）
- ✅ 数据完整性高

### 关闭时保存快照

```java
@Override
public void close() throws IOException {
    logger.info("Closing JodisDb...");
    
    // 只在有数据时才保存快照
    if (!jodisCollection.isEmpty()) {
        try {
            saveSnapshot();  // 保存 JDB
        } catch (Exception e) {
            logger.error("Failed to save snapshot on close", e);
        }
    }
    
    // 关闭 WAL 写入器
    if (walWriter != null) {
        walWriter.close();
        walWriter = null;
    }
    
    // 关闭 JDB 写入器
    if (jdbWriter != null) {
        jdbWriter.close();
        jdbWriter = null;
    }
    
    logger.info("JodisDb closed");
}
```

## 数据恢复测试

### 测试用例

```java
@Test
public void testWalWriteAndRead() throws Exception {
    // 1. 执行写操作
    String setCmd = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nJodis\r\n";
    jodisDb.execute(setCmd);
    
    // 2. 关闭数据库
    jodisDb.close();
    
    // 3. 重新打开
    JodisDb newDb = new JodisDb(config);
    
    // 4. 验证数据恢复
    Assert.assertTrue(newDb.containsKey("name"));
    JodisObject obj = newDb.get("name");
    Assert.assertEquals("Jodis", ((JodisString)obj.getValue()).getHolder());
}

@Test
public void testTtlPersistence() throws Exception {
    // 1. 设置带 TTL 的 Key
    String setexCmd = "*5\r\n$5\r\nSETEX\r\n$4\r\ntemp\r\n$2\r\n60\r\n$5\r\nvalue\r\n";
    jodisDb.execute(setexCmd);
    
    // 2. 保存快照
    jodisDb.saveSnapshot();
    jodisDb.close();
    
    // 3. 重新打开
    JodisDb newDb = new JodisDb(config);
    
    // 4. 验证 TTL 信息被保留
    Assert.assertTrue(newDb.containsKey("temp"));
    Assert.assertTrue(newDb.get("temp").getExpireTime() > 0);
}
```

## 性能优化

### 1. Group Commit（未来优化）

```java
// 批量 fsync，降低 IO 次数
private LinkedBlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();

public void startGroupCommit() {
    new Thread(() -> {
        while (true) {
            List<String> batch = new ArrayList<>();
            writeQueue.drainTo(batch, 100);  // 最多 100 条
            
            if (!batch.isEmpty()) {
                // 一次 fsync 提交所有写入
                flushAndSync();
            }
            
            Thread.sleep(10);  // 每 10ms 提交一次
        }
    }).start();
}
```

### 2. 后台快照（TODO）

```java
// 定时生成快照
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    try {
        saveSnapshot();
    } catch (IOException e) {
        logger.error("Auto snapshot failed", e);
    }
}, 60, 60, TimeUnit.SECONDS);  // 每 60 秒一次
```

### 3. 压缩存储（TODO）

```java
// 使用 Snappy/LZ4 压缩
byte[] compressed = snappy.compress(data);
fos.write(compressed);
```

## 相关文件

- [系统架构](SystemArchitecture.md)
- [内存存储结构](MemoryStorage.md)
- [JodisString](../data-types/JodisString.md)
