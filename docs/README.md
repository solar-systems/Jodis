# Jodis 文档中心

欢迎使用 Jodis 文档！本文档涵盖了从入门到高级的所有内容。

## 📚 文档分类

### 快速开始

- [JodisClient 快速开始](JodisClientQuickStart.md) - 5 分钟上手指南
- [Java 客户端使用](JodisClient.md) - 完整的客户端 API 说明

### 数据类型

- [String（字符串）](data-types/JodisString.md) - 字符串、数字、二进制数据
- [List（列表）](data-types/JodisList.md) - 双向链表，支持两端操作
- [Hash（哈希）](data-types/JodisHash.md) - 键值对集合，适合存储对象
- [Set（集合）](data-types/JodisSet.md) - 无序去重集合
- [SortedSet（有序集合）](data-types/JodisSortedSet.md) - 带分数的排序集合

### Key 操作

- [Key 通用命令](data-types/JodisKey.md) - DEL、EXISTS、TYPE、EXPIRE、TTL 等

### Server 命令

- [Server 命令](data-types/JodisServer.md) - PING、FLUSHDB、DBSIZE 等

### 系统架构

- [系统架构总览](architecture/SystemArchitecture.md) - 6 层架构详解
- [Netty 网络通信](architecture/NettyNetwork.md) - NIO Reactor 模型实现
- [RESP 协议实现](architecture/RespProtocol.md) - Redis 序列化协议详解
- [命令执行框架](architecture/CommandExecutor.md) - 策略模式实现
- [内存存储结构](architecture/MemoryStorage.md) - ConcurrentHashMap + 数据结构
- [持久化机制](architecture/PersistenceLayer.md) - WAL + JDB 双重保障

### 高级主题

- [数据库快照](JodisDataBaseSnapshot.md) - JDB 快照原理
- [WAL 预写日志](WriteAheadLog.md) - 日志持久化机制

## 🎯 推荐阅读顺序

### 初学者路径

1. [JodisClient 快速开始](JodisClientQuickStart.md)
2. [String 数据类型](data-types/JodisString.md)
3. [List 数据类型](data-types/JodisList.md)
4. [Hash 数据类型](data-types/JodisHash.md)
5. [Key 通用命令](data-types/JodisKey.md)

### 进阶开发者路径

1. [系统架构总览](architecture/SystemArchitecture.md)
2. [RESP 协议实现](architecture/RespProtocol.md)
3. [命令执行框架](architecture/CommandExecutor.md)
4. [内存存储结构](architecture/MemoryStorage.md)
5. [持久化机制](architecture/PersistenceLayer.md)

### 贡献者路径

1. [系统架构总览](architecture/SystemArchitecture.md)
2. [Netty 网络通信](architecture/NettyNetwork.md)
3. [命令执行框架](architecture/CommandExecutor.md)
4. 阅读源码 + 提交 PR

## 📖 外部资源

- [GitHub 仓库](https://github.com/solar-systems/Jodis)
- [Redis 官方文档](https://redis.io/documentation)
- [Netty 用户指南](https://netty.io/wiki/user-guide.html)

## ❓ 常见问题

### Jodis 和 Redis 有什么区别？

Jodis 是 Redis 的 Java 实现，兼容 RESP2 协议，但在底层实现上有所不同：
- Redis：C 语言实现，单线程事件循环
- Jodis：Java 实现，Netty NIO 异步 IO

### Jodis 支持集群吗？

当前版本仅支持单机模式，集群模式在规划中（TODO List）。

### 数据会丢失吗？

不会。Jodis 提供 WAL + JDB 双重持久化机制，确保故障恢复时数据不丢失。

### 如何参与贡献？

欢迎提交 Issue 和 Pull Request！查看 [TODO List](README.md#todo-list) 了解待开发功能。

---

**Happy Coding with Jodis! 🎉**
