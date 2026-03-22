# Netty 网络通信实现

Jodis 使用 Netty 框架构建高性能的网络通信层，采用 NIO Reactor 模型。

## 核心组件

### 1. NettySocketServer

服务器启动类，负责创建和配置 Netty Server。

```java
public class NettySocketServer implements Server {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);      // Boss 线程池（接受连接）
        workerGroup = new NioEventLoopGroup();      // Worker 线程池（处理请求）
        
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    
                    // 添加解码器和编码器
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(
                        config.getMaxRequestSize() * 1024, 0, 4, 0, 4));
                    pipeline.addLast(new LengthFieldPrepender(4));
                    pipeline.addLast(new NettyServerHandler());
                }
            });
            
        ChannelFuture future = serverBootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }
}
```

### 2. 长度帧处理

#### LengthFieldBasedFrameDecoder（解码器）

**作用**：解决 TCP 粘包/拆包问题，按长度切分消息。

**参数说明**：
```java
new LengthFieldBasedFrameDecoder(
    maxFrameLength,        // 最大帧长度（防止内存溢出）
    lengthFieldOffset,     // 长度字段偏移量（0 表示从头开始）
    lengthFieldLength,     // 长度字段字节数（4 字节 int）
    lengthAdjustment,      // 长度调整值（0 表示不需要调整）
    initialBytesToStrip    // 跳过的字节数（0 表示保留长度前缀）
)
```

**示例**：
```
原始数据：[4 字节长度][RESP 协议体]
          [0x0000002A][*3\r\n$3\r\nSET\r\n...]
                            ↓
解码后：[*3\r\n$3\r\nSET\r\n...] （去除了长度前缀）
```

#### LengthFieldPrepender（编码器）

**作用**：自动在消息前添加 4 字节长度前缀。

```java
// 发送消息时自动添加长度
pipeline.addLast(new LengthFieldPrepender(4));

// 发送："Hello" → 实际发送：[0x00000005][Hello]
```

### 3. NettyServerHandler

业务处理器，负责将请求分发到后续处理链。

```java
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // 1. 读取请求数据
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        
        // 2. 转换为字符串（RESP 协议）
        String request = new String(bytes, StandardCharsets.UTF_8);
        
        // 3. 创建 Request 对象并传递给下一层
        Request req = respParser.parse(request);
        Response response = jodisDb.execute(req);
        
        // 4. 编码响应并返回
        ByteBuf responseBuf = Unpooled.wrappedBuffer(
            response.encode().getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(responseBuf);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel exception", cause);
        ctx.close();
    }
}
```

## NIO Reactor 模型

### 单线程 Reactor（当前实现）

```
┌─────────────┐
│  Acceptor   │ ← Boss Group (1 个线程)
│ (bind port) │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│      Worker Group (N threads)   │
│  ┌─────────┐ ┌─────────┐ ...    │
│  │Worker 1 │ │Worker 2 │        │
│  │处理所有 │ │处理所有 │        │
│  │IO 操作   │ │IO 操作   │        │
│  └─────────┘ └─────────┘        │
└─────────────────────────────────┘
```

**特点**：
- ✅ 代码简单，易于维护
- ✅ 无线程切换开销
- ✅ 无锁竞争，性能稳定
- ❌ 多核 CPU 利用率低
- ❌ 单个连接阻塞会影响其他连接

### 多线程 Reactor（未来优化）

```
┌─────────────┐
│  Acceptor   │ ← Boss Group
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│      Worker Group (M threads)   │
│  ┌─────────┐ ┌─────────┐ ...    │
│  │Worker 1 │ │Worker 2 │        │
│  │处理部分 │ │处理部分 │        │
│  │连接     │ │连接     │        │
│  └─────────┘ └─────────┘        │
└─────────────────────────────────┘
```

## 性能优化点

### 1. 零拷贝（Zero-Copy）

```java
// 使用直接内存，避免 JVM 堆内存复制
ByteBuf buffer = ctx.alloc().directBuffer(size);

// 合并多个小缓冲区
CompositeByteBuf composite = ctx.alloc().compositeBuffer();
composite.addComponent(true, buffer1);
composite.addComponent(true, buffer2);
```

### 2. 内存池化

```java
// Netty 自动管理内存池
ByteBuf buffer = ctx.alloc().buffer();  // 从池中分配

// 使用后自动归还到池中
buffer.release();
```

### 3. 异步 IO

```java
// 非阻塞写操作
ctx.writeAndFlush(response).addListener(future -> {
    if (!future.isSuccess()) {
        logger.error("Write failed", future.cause());
    }
});
```

## 配置文件

```properties
# conf/jodis.properties
server.port=6059
server.max.request=1024        # 最大请求大小（KB）
server.max.concurrency=64      # 最大并发连接数
server.type=nio                # IO 模型：nio / aio
```

## 相关文件

- [系统架构](SystemArchitecture.md)
- [RESP 协议详解](RespProtocol.md)
- [命令执行框架](CommandExecutor.md)
