# Jodis 系统架构

Jodis 采用经典的分层架构设计，各层职责清晰，易于理解和扩展。

## 整体架构图

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

## 架构分层说明

### 1. 客户端层（Client Layer）
- **支持多种客户端**：Java Client、Redis CLI、其他任何兼容 RESP 协议的客户端
- **协议统一**：所有客户端通过 RESP 协议与服务器通信

### 2. 网络层（Network Layer）
- **基于 Netty**：使用 NIO Reactor 模型，高性能异步 IO
- **长度帧解码器**：`LengthFieldBasedFrameDecoder` 处理粘包/拆包
- **长度帧编码器**：`LengthFieldPrepender` 添加 4 字节长度前缀
- **请求处理器**：`RequestHandler` 分发请求到协议层

### 3. 协议层（Protocol Layer）
- **RESP Parser**：解析 RESP 协议格式的请求
- **Response Handler**：编码响应并返回给客户端
- **错误处理**：统一的异常处理和错误响应生成

### 4. 命令执行层（Command Executor Layer）
- **按数据类型划分**：每个 Executor 负责一种数据类型的命令
  - `StringExecutor`：处理 String 相关命令
  - `ListExecutor`：处理 List 相关命令
  - `HashExecutor`：处理 Hash 相关命令
  - `SetExecutor`：处理 Set 相关命令
  - `SortedSetExecutor`：处理 SortedSet 相关命令
  - `KeyExecutor`：处理 Key 通用操作
  - `ServerExecutor`：处理 Server 相关命令
- **策略模式**：每个命令对应一个策略类，易于扩展

### 5. 存储层（Storage Layer）
- **纯内存存储**：基于 `ConcurrentHashMap<String, ExpireObject>`
- **过期时间管理**：`ExpireObject` 封装值和过期时间
- **有序集合**：`SkipList`（跳跃表）实现 SortedSet

### 6. 持久化层（Persistence Layer）
- **WAL（Write-Ahead Log）**：预写日志，记录所有写操作
- **JDB（Jodis DataBase Dump）**：定期数据快照，快速恢复
- **双重保障**：WAL + JDB 确保数据不丢失

## 设计原则

1. **单一职责**：每层只负责一个明确的功能
2. **高内聚低耦合**：层与层之间通过接口交互
3. **易于扩展**：新增功能只需修改对应的层
4. **性能优先**：纯内存操作 + 异步 IO

## 相关文件

- [网络通信实现](NettyNetwork.md)
- [RESP 协议详解](RespProtocol.md)
- [命令执行框架](CommandExecutor.md)
- [内存存储结构](MemoryStorage.md)
- [持久化机制](PersistenceLayer.md)
