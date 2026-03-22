package cn.abelib.jodis.example;

import cn.abelib.jodis.client.JodisClient;
import cn.abelib.jodis.client.JodisClientConfig;

import java.io.IOException;
import java.util.List;

/**
 * Jodis 客户端使用示例
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClientExample {
    public static void main(String[] args) {
        // 创建客户端配置
        JodisClientConfig config = new JodisClientConfig("localhost", 6059);
        
        // 创建客户端实例
        try (JodisClient client = new JodisClient(config)) {
            // 连接服务器
            client.connect();
            
            System.out.println("=== Jodis Client Example ===\n");
            
            // 1. PING 测试
            System.out.println("1. Testing PING:");
            String pingResponse = client.ping();
            System.out.println("   PING -> " + pingResponse);
            System.out.println();
            
            // 2. String 操作
            System.out.println("2. String Operations:");
            client.set("name", "Jodis");
            System.out.println("   SET name Jodis");
            
            String name = client.get("name");
            System.out.println("   GET name -> " + name);
            
            client.set("counter", "100");
            long counter = client.incr("counter");
            System.out.println("   INCR counter -> " + counter);
            
            long strlen = client.strlen("name");
            System.out.println("   STRLEN name -> " + strlen);
            System.out.println();
            
            // 3. MSET/MGET 批量操作
            System.out.println("3. Batch Operations:");
            client.mset("key1", "value1", "key2", "value2", "key3", "value3");
            System.out.println("   MSET key1 value1 key2 value2 key3 value3");
            
            List<String> values = client.mget("key1", "key2", "key3");
            System.out.println("   MGET key1 key2 key3 -> " + values);
            System.out.println();
            
            // 4. Hash 操作
            System.out.println("4. Hash Operations:");
            client.hset("user:1", "name", "Alice");
            client.hset("user:1", "age", "25");
            client.hset("user:1", "city", "Beijing");
            System.out.println("   HSET user:1 name Alice");
            System.out.println("   HSET user:1 age 25");
            System.out.println("   HSET user:1 city Beijing");
            
            String userName = client.hget("user:1", "name");
            System.out.println("   HGET user:1 name -> " + userName);
            
            List<String> allFields = client.hgetall("user:1");
            System.out.println("   HGETALL user:1 -> " + allFields);
            System.out.println();
            
            // 5. List 操作
            System.out.println("5. List Operations:");
            client.lpush("mylist", "item3", "item2", "item1");
            System.out.println("   LPUSH mylist item3 item2 item1");
            
            List<String> listItems = client.lrange("mylist", 0, -1);
            System.out.println("   LRANGE mylist 0 -1 -> " + listItems);
            
            String lpop = client.lpop("mylist");
            System.out.println("   LPOP mylist -> " + lpop);
            System.out.println();
            
            // 6. Set 操作
            System.out.println("6. Set Operations:");
            client.sadd("myset", "apple", "banana", "orange");
            System.out.println("   SADD myset apple banana orange");
            
            List<String> setMembers = client.smembers("myset");
            System.out.println("   SMEMBERS myset -> " + setMembers);
            
            long isMember = client.sismember("myset", "apple");
            System.out.println("   SISMEMBER myset apple -> " + (isMember == 1 ? "true" : "false"));
            
            long setSize = client.scard("myset");
            System.out.println("   SCARD myset -> " + setSize);
            System.out.println();
            
            // 7. Sorted Set 操作
            System.out.println("7. Sorted Set Operations:");
            client.zadd("leaderboard", 100, "Player1");
            client.zadd("leaderboard", 250, "Player2");
            client.zadd("leaderboard", 150, "Player3");
            System.out.println("   ZADD leaderboard 100 Player1");
            System.out.println("   ZADD leaderboard 250 Player2");
            System.out.println("   ZADD leaderboard 150 Player3");
            
            List<String> topPlayers = client.zrange("leaderboard", 0, -1);
            System.out.println("   ZRANGE leaderboard 0 -1 -> " + topPlayers);
            
            String score = client.zscore("leaderboard", "Player2");
            System.out.println("   ZSCORE leaderboard Player2 -> " + score);
            
            long zsetSize = client.zcard("leaderboard");
            System.out.println("   ZCARD leaderboard -> " + zsetSize);
            System.out.println();
            
            // 8. Key 操作
            System.out.println("8. Key Operations:");
            long exists = client.exists("name", "mylist", "nonexistent");
            System.out.println("   EXISTS name mylist nonexistent -> " + exists);
            
            String type = client.type("mylist");
            System.out.println("   TYPE mylist -> " + type);
            
            List<String> allKeys = client.keys("*");
            System.out.println("   KEYS * -> " + allKeys);
            System.out.println();
            
            // 8.5 TTL 操作测试
            System.out.println("8.5 TTL Operations:");
            client.set("temp_key", "temp_value");
            System.out.println("   SET temp_key temp_value");
            
            long expireResult = client.expire("temp_key", 60);  // 设置 60 秒过期
            System.out.println("   EXPIRE temp_key 60 -> " + (expireResult == 1 ? "success" : "failed"));
            
            long ttl = client.ttl("temp_key");
            System.out.println("   TTL temp_key -> " + ttl + " seconds");
            
            // 测试 SETEX
            client.setex("session_key", 120, "session_data");
            System.out.println("   SETEX session_key 120 session_data");
            
            long sessionTtl = client.ttl("session_key");
            System.out.println("   TTL session_key -> " + sessionTtl + " seconds");
            
            String sessionValue = client.get("session_key");
            System.out.println("   GET session_key -> " + sessionValue);
            
            // 测试永不过期的 key
            client.set("permanent_key", "permanent_value");
            long permanentTtl = client.ttl("permanent_key");
            System.out.println("   TTL permanent_key -> " + permanentTtl + " (-1 means no expiry)");
            
            // 测试不存在的 key
            long nonExistentTtl = client.ttl("non_existent_key");
            System.out.println("   TTL non_existent_key -> " + nonExistentTtl + " (-2 means not exists)");
            System.out.println();
            
            // 9. Server 操作
            System.out.println("9. Server Operations:");
            long dbSize = client.dbsize();
            System.out.println("   DBSIZE -> " + dbSize);
            System.out.println();
            
            // 10. 删除数据
            System.out.println("10. Cleanup:");
            long deleted = client.del("name", "counter", "mylist", "myset", "leaderboard");
            System.out.println("   DEL name counter mylist myset leaderboard -> " + deleted);
            
            client.flushdb();
            System.out.println("   FLUSHDB (cleared database)");
            System.out.println();
            
            System.out.println("=== Example Completed ===");
            
        } catch (IOException e) {
            System.err.println("Error connecting to Jodis server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
