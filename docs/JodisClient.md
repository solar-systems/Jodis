# Jodis Client

Jodis Java 客户端，提供简单易用的 API 来连接和操作 Jodis 服务器。

## 快速开始

### 1. 启动 Jodis 服务器

确保 Jodis 服务器已经启动并运行在 `localhost:6059` 端口。

```bash
# 使用默认配置启动
java -cp your-classpath cn.abelib.jodis.Jodis
```

### 2. 添加依赖

在项目的 `pom.xml` 中添加（如果是 Maven 项目）：

```xml
<dependencies>
    <!-- Jodis 核心依赖 -->
    <dependency>
        <groupId>cn.abelib</groupId>
        <artifactId>jodis</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Guava (Jodis 依赖) -->
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>29.0-jre</version>
    </dependency>
</dependencies>
```

### 3. 创建客户端

```java
import cn.abelib.jodis.client.JodisClient;
import cn.abelib.jodis.client.JodisClientConfig;

// 方式 1: 使用默认配置 (localhost:6059)
JodisClient client = new JodisClient("localhost", 6059);

// 方式 2: 使用配置对象
JodisClientConfig config = new JodisClientConfig("localhost", 6059);
JodisClient client = new JodisClient(config);

// 连接服务器
client.connect();
```

### 4. 执行命令

#### String 操作

```java
// SET
client.set("name", "Jodis");

// GET
String name = client.get("name");

// INCR
client.set("counter", "100");
long counter = client.incr("counter"); // 101

// DECR
long counter = client.decr("counter"); // 100

// MSET / MGET
client.mset("key1", "value1", "key2", "value2");
List<String> values = client.mget("key1", "key2");
```

#### Hash 操作

```java
// HSET
client.hset("user:1", "name", "Alice");
client.hset("user:1", "age", "25");

// HGET
String name = client.hget("user:1", "name"); // "Alice"

// HMSET
client.hmset("user:1", "city", "Beijing", "country", "China");

// HMGET
List<String> values = client.hmget("user:1", "name", "age", "city");

// HGETALL
List<String> allFields = client.hgetall("user:1");

// HDEL
client.hdel("user:1", "age");
```

#### List 操作

```java
// LPUSH
client.lpush("mylist", "item1", "item2", "item3");

// RPUSH
client.rpush("mylist", "item4");

// LRANGE
List<String> items = client.lrange("mylist", 0, -1);

// LPOP / RPOP
String left = client.lpop("mylist");
String right = client.rpop("mylist");

// LINDEX
String item = client.lindex("mylist", 0);
```

#### Set 操作

```java
// SADD
client.sadd("myset", "apple", "banana", "orange");

// SMEMBERS
List<String> members = client.smembers("myset");

// SISMEMBER
boolean exists = client.sismember("myset", "apple") == 1;

// SREM
client.srem("myset", "banana");

// SCARD
long size = client.scard("myset");
```

#### Sorted Set 操作

```java
// ZADD
client.zadd("leaderboard", 100, "Player1");
client.zadd("leaderboard", 250, "Player2");

// ZRANGE
List<String> players = client.zrange("leaderboard", 0, -1);

// ZSCORE
String score = client.zscore("leaderboard", "Player2"); // "250"

// ZREM
client.zrem("leaderboard", "Player1");

// ZCARD
long size = client.zcard("leaderboard");
```

#### Key 操作

```java
// DEL
client.del("key1", "key2", "key3");

// EXISTS
long exists = client.exists("key1", "key2"); // 2

// TYPE
String type = client.type("mylist"); // "list"

// KEYS
List<String> keys = client.keys("*");
```

#### Server 操作

```java
// PING
String pong = client.ping(); // "PONG"

// FLUSHDB
client.flushdb();

// DBSIZE
long size = client.dbsize();
```

### 5. 关闭连接

```java
// 使用 try-with-resources
try (JodisClient client = new JodisClient("localhost", 6059)) {
    client.connect();
    // ... 使用客户端
}

// 或手动关闭
client.close();
```

## 完整示例

参考 `example/JodisClientExample.java`:

```java
import cn.abelib.jodis.client.JodisClient;
import cn.abelib.jodis.client.JodisClientConfig;

public class MyApplication {
    public static void main(String[] args) {
        try (JodisClient client = new JodisClient("localhost", 6059)) {
            client.connect();
            
            // 存储数据
            client.set("greeting", "Hello, Jodis!");
            
            // 读取数据
            String greeting = client.get("greeting");
            System.out.println(greeting); // Hello, Jodis!
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 运行测试

```bash
# 确保 Jodis 服务器正在运行
mvn test -Dtest=JodisClientTest
```

## API 文档

### JodisClient 主要方法

| 分类 | 方法 | 说明 |
|------|------|------|
| **连接** | `connect()` | 连接到服务器 |
| | `isConnected()` | 检查连接状态 |
| | `close()` | 关闭连接 |
| **String** | `set(key, value)` | 设置字符串值 |
| | `get(key)` | 获取字符串值 |
| | `incr(key)` | 自增 1 |
| | `decr(key)` | 自减 1 |
| | `mset(...)` | 批量设置 |
| | `mget(...)` | 批量获取 |
| **Hash** | `hset(key, field, value)` | 设置哈希字段 |
| | `hget(key, field)` | 获取哈希字段 |
| | `hmset(key, ...)` | 批量设置字段 |
| | `hmget(key, ...)` | 批量获取字段 |
| | `hgetall(key)` | 获取所有字段 |
| | `hdel(key, ...)` | 删除字段 |
| **List** | `lpush(key, ...)` | 左侧插入 |
| | `rpush(key, ...)` | 右侧插入 |
| | `lpop(key)` | 左侧弹出 |
| | `rpop(key)` | 右侧弹出 |
| | `lrange(key, start, end)` | 获取范围 |
| **Set** | `sadd(key, ...)` | 添加成员 |
| | `smembers(key)` | 获取所有成员 |
| | `sismember(key, member)` | 检查成员 |
| | `srem(key, ...)` | 删除成员 |
| **ZSet** | `zadd(key, score, member)` | 添加成员 |
| | `zrange(key, start, end)` | 获取排名范围 |
| | `zscore(key, member)` | 获取分数 |
| | `zrem(key, ...)` | 删除成员 |
| **Key** | `del(...keys)` | 删除键 |
| | `exists(...keys)` | 检查存在 |
| | `type(key)` | 获取类型 |
| | `keys(pattern)` | 匹配键 |
| **Server** | `ping()` | 测试连接 |
| | `flushdb()` | 清空数据库 |
| | `dbsize()` | 数据库大小 |

## 注意事项

1. **连接管理**: 使用完毕后请关闭连接，推荐使用 try-with-resources
2. **异常处理**: 所有操作都可能抛出 IOException，请妥善处理
3. **线程安全**: 当前实现不是线程安全的，多线程环境下请使用连接池
4. **超时设置**: 默认连接和读取超时为 5 秒，可通过配置调整

## 配置选项

```java
JodisClientConfig config = new JodisClientConfig();
config.setHost("localhost");      // 服务器地址
config.setPort(6059);             // 服务器端口
config.setConnectionTimeout(5000); // 连接超时 (毫秒)
config.setSoTimeout(5000);        // 读取超时 (毫秒)
```

## 支持的 RESP 协议

客户端完全兼容 Redis RESP2 协议，支持以下响应类型：
- Simple Strings (+)
- Errors (-)
- Integers (:)
- Bulk Strings ($)
- Arrays (*)

