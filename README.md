# Jodis - Java Object Dictionary Server

Jodis 是一个高性能的基于内存的 NoSQL 键值数据库，采用 Java 开发，支持独立部署和嵌入式使用。兼容 Redis RESP2 协议，提供丰富的数据结构和持久化能力。

[![Java CI with Maven](https://github.com/solar-systems/Jodis/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/solar-systems/Jodis/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 目录

- [系统架构](#-系统架构)
- [核心功能](#-核心功能)
- [使用方式](#-使用方式)
- [底层实现](#-底层实现)
- [性能特点](#-性能特点)
- [快速开始](#-快速开始)
- [文档](#-文档)

---

## 🏗️ 系统架构

### 整体架构图

```
┌────────────────────────────────────────────────────────┐
│                      Client Layer                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │Java Client  │  │Redis CLI    │  │Other Clients│     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
└─────────┼────────────────┼────────────────┼────────────┘
          │                │                │
          └────────────────┴────────────────┘
                   RESP Protocol (TCP)
                         │
┌────────────────────────▼────────────────────────────────┐
│                    Network Layer                        │
│  ┌─────────────────────────────────────────────────┐    │
│  │         Netty Server (NIO Reactor)              │    │
│  │  - LengthFieldBasedFrameDecoder                 │    │
│  │  - LengthFieldPrepender                         │    │
│  │  - RequestHandler                               │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Protocol Layer                        │
│  ┌─────────────────────────────────────────────────┐    │
│  │            RESP Parser & Handler                │    │
│  │  - Request 解析                                  │    │
│  │  - Response 编码                                 │    │
│  │  - 错误处理                                      │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Command Executor Layer                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Executor (按数据类型分类)                        │    │
│  │  - StringExecutor    - HashExecutor             │    │
│  │  - ListExecutor      - SetExecutor              │    │
│  │  - SortedSetExecutor - KeyExecutor              │    │
│  │  - ServerExecutor                               │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Storage Layer                         │
│  ┌────────────────────────────────────────────────┐     │
│  │              JodisDb (内存存储)                  │    │
│  │  - ConcurrentHashMap 存储数据                    │    │
│  │  - ExpireObject 过期时间管理                      │    │
│  │  - SkipList 有序集合（跳跃表）                     │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                 Persistence Layer                       │
│  ┌──────────────┐         ┌──────────────┐              │
│  │   WAL Log    │         │   JDB Dump   │              │
│  │ (预写日志)    │         │ (数据快照)    │               │
│  │ WalWriter    │         │ JdbWriter    │              │
│  │ WalReader    │         │ JdbReader    │              │
│  └──────────────┘         └──────────────┘              │
└─────────────────────────────────────────────────────────┘
```

### 架构分层说明

1. **客户端层**：支持 Java 客户端、Redis CLI 等任何兼容 RESP 协议的客户端
2. **网络层**：基于 Netty 的 NIO Reactor 模型，高性能异步 IO
3. **协议层**：完整实现 RESP2 协议的编解码
4. **命令执行层**：按数据类型划分的 Executor，职责清晰
5. **存储层**：纯内存存储，ConcurrentHashMap + 自定义数据结构
6. **持久化层**：WAL 日志 + JDB 快照双重保障

---

## ✨ 核心功能

### 1. 丰富的数据结构

| 数据类型 | 说明 | 底层实现 |
|---------|------|----------|
| **String** | 字符串、数字、二进制数据 | `java.lang.String` |
| **List** | 双向链表，支持两端操作 | `java.util.LinkedList` |
| **Hash** | 哈希表，适合存储对象 | `java.util.HashMap` |
| **Set** | 无序去重集合 | `java.util.HashSet` |
| **SortedSet** | 有序集合，按分数排序 | `TreeMap` + `SkipList` |

### 2. 完整的命令支持

#### Server 命令
- `PING` - 连接测试
- `FLUSHDB` - 清空数据库
- `DBSIZE` - 获取键数量

#### Key 命令
- `DEL`, `EXISTS`, `TYPE` - 基本操作
- `EXPIRE`, `EXPIREAT`, `TTL` - 过期时间
- `KEYS`, `RANDOMKEY` - 键查询
- `RENAME`, `RENAMENX` - 重命名

#### String 命令
- `GET`, `SET`, `GETSET` - 基本操作
- `MGET`, `MSET` - 批量操作
- `INCR`, `DECR`, `INCRBY`, `DECRBY` - 自增自减
- `STRLEN`, `APPEND`, `GETRANGE`, `SETRANGE` - 字符串操作
- `SETEX`, `SETNX` - 条件设置

#### List 命令
- `LPUSH`, `RPUSH` - 插入元素
- `LPOP`, `RPOP` - 弹出元素
- `LRANGE`, `LINDEX`, `LSET` - 查询操作
- `LINSERT` - 插入操作

#### Hash 命令
- `HSET`, `HGET`, `HMSET`, `HMGET` - 基本操作
- `HGETALL`, `HKEYS`, `HVALS` - 全量查询
- `HDEL`, `HEXISTS` - 删除检查
- `HINCRBY`, `HINCRBYFLOAT` - 增量操作
- `HLEN`, `HSCAN` - 统计迭代

#### SortedSet 命令
- `ZADD`, `ZREM` - 添加删除
- `ZRANGE`, `ZCARD`, `ZCOUNT` - 范围统计
- `ZSCORE`, `ZRANK` - 分数排名

### 3. 数据持久化

#### WAL (Write-Ahead Log)
- **机制**：所有写操作先记录日志，再更新内存
- **优势**：故障恢复，数据不丢失
- **配置**：自动重写，避免文件过大

#### JDB (Jodis DataBase Dump)
- **机制**：定期生成数据快照
- **格式**：二进制压缩存储
- **恢复**：启动时加载，快速恢复

### 4. 灵活部署模式

#### 独立服务器模式
```
java -cp jodis.jar cn.abelib.jodis.Jodis conf/jodis.properties
```

#### 嵌入式模式
```
EmbaddedJodis jodis = EmbaddedJodis.start("conf/jodis.properties");
Response response = jodis.execute(new Request("GET", "key"));
```

---

## 🚀 使用方式

### 1. 启动服务器

```
# 克隆项目
git clone https://github.com/abel-huang/Jodis.git
cd Jodis

# 编译
mvn clean package -DskipTests

# 启动服务器
java -cp target/classes:target/test-classes:$(cat .classpath.deps) \
    cn.abelib.jodis.Jodis conf/jodis.properties
```

### 2. 使用 Java 客户端

```
import cn.abelib.jodis.client.JodisClient;
import cn.abelib.jodis.client.JodisClientConfig;

public class MyApp {
    public static void main(String[] args) throws Exception {
        try (JodisClient client = new JodisClient("localhost", 6059)) {
            client.connect();
            
            // String 操作
            client.set("name", "Jodis");
            String name = client.get("name");
            System.out.println(name); // 输出：Jodis
            
            // Hash 操作
            client.hset("user:1", "name", "Alice");
            client.hset("user:1", "age", "25");
            
            // List 操作
            client.lpush("mylist", "item1", "item2", "item3");
            
            // 关闭连接
            client.close();
        }
    }
}
```

### 3. 使用 Redis CLI

```
# 安装 redis-cli
redis-cli -h localhost -p 6059

# 测试
127.0.0.1:6059> PING
PONG

127.0.0.1:6059> SET greeting "Hello, Jodis!"
OK

127.0.0.1:6059> GET greeting
"Hello, Jodis!"

127.0.0.1:6059> LPUSH mylist item1 item2 item3
(integer) 3

127.0.0.1:6059> LRANGE mylist 0 -1
1) "item3"
2) "item2"
3) "item1"
```

### 4. 运行示例程序

```
# 运行客户端示例
java -cp target/classes:target/test-classes:$(cat .classpath.deps) \
    cn.abelib.jodis.example.JodisClientExample
```

---

## 🔧 底层实现

详细的底层实现文档请查看 [docs/architecture](docs/architecture) 目录。

### 架构分层概览

Jodis 采用经典的分层架构设计，共分为 6 层：

1. **客户端层**：支持 Java Client、Redis CLI 等任何兼容 RESP 协议的客户端
2. **网络层**：基于 Netty 的 NIO Reactor 模型，高性能异步 IO
3. **协议层**：完整实现 RESP2 协议的编解码
4. **命令执行层**：按数据类型划分的 Executor，职责清晰
5. **存储层**：纯内存存储，ConcurrentHashMap + 自定义数据结构
6. **持久化层**：WAL 日志 + JDB 快照双重保障

完整的架构图和详细说明请查看：
- **[系统架构总览](docs/architecture/SystemArchitecture.md)**
- **[Netty 网络通信](docs/architecture/NettyNetwork.md)**
- **[RESP 协议详解](docs/architecture/RespProtocol.md)**
- **[命令执行框架](docs/architecture/CommandExecutor.md)**
- **[内存存储结构](docs/architecture/MemoryStorage.md)**
- **[持久化机制](docs/architecture/PersistenceLayer.md)**

---

## ⚡ 性能特点

### 设计优势

1. **纯内存操作** - 微秒级响应时间
2. **单线程模型** - 无线程切换开销，无锁竞争
3. **零拷贝** - 直接内存操作，减少数据复制
4. **批量优化** - MSET/MGET 减少网络往返
5. **惰性删除** - 过期键访问时删除，降低 CPU 开销

### 适用场景

✅ **适合**：
- 缓存层
- 会话存储
- 实时排行榜
- 消息队列
- 计数器

❌ **不适合**：
- 大数据量（受内存限制）
- 高并发写入（单线程瓶颈）
- 复杂查询（仅支持简单 KV）

---

## 🎯 快速开始

### 环境要求
- JDK 8+
- Maven 3.x

### 配置文件

```
# conf/jodis.properties
jodis.port=6059
log.dir=log/
log.jdb=default.jdb
log.wal=default.wal
log.wal.rewrite.size=64 * 1024 * 1024
log.reload.mode=2
server.max.request=1024
server.max.concurrency=64
server.type=nio
```

### 运行测试

```
# 单元测试
mvn test

# 客户端测试
mvn test -Dtest=JodisClientTest
```

---

## 📚 文档

### 完整文档中心

所有文档已整理到 [docs](docs) 目录，包含：

**快速开始**
- [JodisClient 快速开始](docs/JodisClientQuickStart.md)
- [Java 客户端使用](docs/JodisClient.md)

**数据类型**
- [String（字符串）](docs/data-types/JodisString.md)
- [List（列表）](docs/data-types/JodisList.md)
- [Hash（哈希）](docs/data-types/JodisHash.md)
- [Set（集合）](docs/data-types/JodisSet.md)
- [SortedSet（有序集合）](docs/data-types/JodisSortedSet.md)
- [Key 通用命令](docs/data-types/JodisKey.md)
- [Server 命令](docs/data-types/JodisServer.md)

**系统架构**
- [系统架构总览](docs/architecture/SystemArchitecture.md)
- [Netty 网络通信](docs/architecture/NettyNetwork.md)
- [RESP 协议详解](docs/architecture/RespProtocol.md)
- [命令执行框架](docs/architecture/CommandExecutor.md)
- [内存存储结构](docs/architecture/MemoryStorage.md)
- [持久化机制](docs/architecture/PersistenceLayer.md)

**高级主题**
- [数据库快照](docs/JodisDataBaseSnapshot.md)
- [WAL 预写日志](docs/WriteAheadLog.md)

---

## 📋 TODO List
- [ ] LRU 淘汰算法
- [ ] IO 多路复用优化
- [ ] WAL Rewrite 后台任务
- [ ] JDB 定时快照任务
- [ ] 高可用和集群模式探索
- [ ] JMH 性能基准测试
- [ ] Docker 镜像支持
---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👤 作者

Abel Huang (abel.huang)

---

**Happy Coding with Jodis! 🎉**
