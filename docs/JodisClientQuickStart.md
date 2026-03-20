# Jodis Client 快速开始

## 已创建的文件

### 核心类文件

1. **JodisClient.java** - 主客户端类，提供高级 API
   - 支持所有 Jodis 数据类型操作（String, Hash, List, Set, Sorted Set）
   - 自动连接管理
   - 异常处理

2. **JodisConnection.java** - 底层连接类
   - Socket 连接管理
   - RESP 协议编解码
   - 超时控制

3. **JodisClientConfig.java** - 配置类
   - 主机、端口配置
   - 超时时间设置

4. **JodisClientFactory.java** - 工厂类
   - 从配置文件创建客户端
   - 从 Properties 对象创建客户端

### 示例和测试

5. **JodisClientExample.java** - 完整使用示例
   - 演示所有基本操作
   - 可直接运行查看效果

6. **JodisClientTest.java** - 单元测试
   - 覆盖所有 API
   - 验证功能正确性

### 配置文件

7. **conf/jodis-client.properties** - 客户端配置文件
   - 服务器地址和端口
   - 超时时间设置

8. **conf/jodis-client.properties.sample** - 配置文件模板

### 文档

9. **README.md** - API 参考文档
   - 完整的 API 说明
   - 使用示例代码
   - 方法列表

10. **CLIENT_GUIDE.md** - 详细使用指南
    - 快速入门教程
    - 最佳实践
    - 常见问题解答

## 快速使用

### 1. 启动 Jodis 服务器

确保服务器已启动并运行在 `localhost:6059`

```bash
java -cp target/classes:target/test-classes:$(cat .classpath.deps) \
    cn.abelib.jodis.Jodis conf/jodis.properties
```

### 2. 运行示例程序

```bash
# 方式 1: 直接运行 Java
cd /Users/abel/IdeaProjects/Jodis
java -cp target/classes:target/test-classes:$(find ~/.m2/repository -name "*.jar" | tr '\n' ':') \
    cn.abelib.jodis.example.JodisClientExample

# 方式 2: 使用 Maven
mvn compile exec:java -Dexec.mainClass="cn.abelib.jodis.example.JodisClientExample"
```

### 3. 运行测试

```bash
mvn test -Dtest=JodisClientTest
```

### 4. 在自己的项目中使用

```java
import cn.abelib.jodis.client.JodisClient;

public class MyApp {
    public static void main(String[] args) throws Exception {
        try (JodisClient client = new JodisClient("localhost", 6059)) {
            client.connect();
            
            // String 操作
            client.set("name", "MyApp");
            String name = client.get("name");
            System.out.println(name);
            
            // Hash 操作
            client.hset("user:1", "name", "Alice");
            client.hset("user:1", "age", "25");
            
            // List 操作
            client.lpush("mylist", "item1", "item2");
            
            // ... 更多操作
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 支持的命令

### String (字符串)
- SET, GET, GETSET, MSET, MGET
- INCR, DECR, INCRBY, DECRBY
- STRLEN, APPEND

### Hash (哈希)
- HSET, HGET, HMSET, HMGET, HGETALL
- HDEL, HEXISTS, HKEYS, HVALS

### List (列表)
- LPUSH, RPUSH, LPOP, RPOP
- LRANGE, LINDEX

### Set (集合)
- SADD, SMEMBERS, SISMEMBER
- SREM, SCARD

### Sorted Set (有序集合)
- ZADD, ZRANGE, ZSCORE, ZREM, ZCARD

### Key (键)
- DEL, EXISTS, TYPE, KEYS

### Server (服务器)
- PING, FLUSHDB, DBSIZE

## 项目结构

```
src/main/java/cn/abelib/jodis/client/
├── JodisClient.java           # 主客户端类
├── JodisConnection.java       # 连接管理类
├── JodisClientConfig.java     # 配置类
├── JodisClientFactory.java    # 工厂类
├── README.md                  # API 文档
└── CLIENT_GUIDE.md            # 使用指南

src/main/java/cn/abelib/jodis/example/
└── JodisClientExample.java    # 示例代码

src/test/java/cn/abelib/jodis/client/
└── JodisClientTest.java       # 单元测试

conf/
├── jodis-client.properties    # 客户端配置
└── jodis-client.properties.sample  # 配置模板

docs/
└── JodisClient.md             # 客户端文档
```

## 特性

✅ **简单易用**: 高级 API 设计，类似 Redis 客户端
✅ **功能完整**: 支持所有 Jodis 数据类型
✅ **RESP 协议**: 完全兼容 Redis RESP2 协议
✅ **连接管理**: 自动连接和超时处理
✅ **批量操作**: 支持 MSET, MGET, HMSET, HMGET 等
✅ **异常处理**: 完善的 IOException 处理
✅ **资源管理**: 支持 try-with-resources 自动关闭
✅ **文档完善**: 详细的 API 文档和使用指南

## 注意事项

1. **确保服务器运行**: 使用前请确保 Jodis 服务器已启动
2. **及时关闭连接**: 使用 try-with-resources 或手动调用 close()
3. **非线程安全**: 当前实现不是线程安全的
4. **异常处理**: 所有操作都可能抛出 IOException

## 下一步

- 阅读 `CLIENT_GUIDE.md` 了解详细使用方法
- 查看 `example/JodisClientExample.java` 学习示例代码
- 运行 `JodisClientTest` 单元测试验证功能
- 根据需求扩展客户端功能

