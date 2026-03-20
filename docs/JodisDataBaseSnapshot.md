## Jodis DataBase Snapshot

### 概述
Jodis 支持数据库快照功能，可以将当前内存中的数据持久化到磁盘文件中。

### SAVE
同步保存数据库快照到磁盘。该命令会阻塞服务器，直到保存完成。

**语法**：
```
SAVE
```

**响应**：
- 成功：`OK`
- 失败：错误信息

### BGSAVE
异步保存数据库快照到磁盘。该命令会在后台线程中执行，不会阻塞服务器。

**语法**：
```
BGSAVE
```

**响应**：
- 后台保存任务已启动：`Background saving started`
- 已有保存任务在运行：`Background saving already in progress`

### LASTSAVE
返回上次成功保存数据库的时间戳（UNIX 时间格式）。

**语法**：
```
LASTSAVE
```

**响应**：
- UNIX 时间戳（秒）

### 数据持久化机制
1. **RDB 文件**：数据以 RDB（Redis Database）格式保存到 `.jdb` 文件中
2. **WAL 日志**：Write-Ahead Log 记录所有写操作，用于故障恢复
3. **自动保存**：可配置定期自动保存

### 配置文件相关参数
```properties
# 数据文件目录
log.dir=log/

# 数据库文件名
log.jdb=default.jdb

# WAL 日志文件名
log.wal=default.wal

# WAL 重写阈值
log.wal.rewrite.size=64 * 1024 * 1024

# 重载模式
log.reload.mode=2
```

### 使用示例
```bash
# 手动触发保存
SAVE

# 查看上次保存时间
LASTSAVE
# 返回：1711008000

# 异步保存
BGSAVE
# 返回：Background saving started
```

### 注意事项
1. SAVE 命令会阻塞服务器，生产环境建议使用 BGSAVE
2. 定期检查 LASTSAVE 确保数据正常保存
3. 确保磁盘空间充足
4. 备份 RDB 和 WAL 文件以防止数据丢失