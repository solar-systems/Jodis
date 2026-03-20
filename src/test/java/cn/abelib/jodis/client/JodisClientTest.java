package cn.abelib.jodis.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Jodis 客户端测试
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClientTest {
    private JodisClient client;

    @Before
    public void setUp() throws IOException {
        // 创建客户端并连接
        client = new JodisClient("localhost", 6059);
        client.connect();
    }

    @After
    public void tearDown() throws IOException {
        // 清理测试数据
        if (client != null) {
            try {
                client.flushdb();
                client.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    @Test
    public void testPing() throws IOException {
        String response = client.ping();
        assertEquals("PONG", response);
    }

    @Test
    public void testSetAndGet() throws IOException {
        // SET
        String setResult = client.set("test:key", "hello");
        assertEquals("OK", setResult);

        // GET
        String getValue = client.get("test:key");
        assertEquals("hello", getValue);
    }

    @Test
    public void testIncrDecr() throws IOException {
        client.set("counter", "10");
        
        long incrResult = client.incr("counter");
        assertEquals(11, incrResult);
        
        long decrResult = client.decr("counter");
        assertEquals(10, decrResult);
    }

    @Test
    public void testMsetMget() throws IOException {
        // MSET
        String msetResult = client.mset("key1", "value1", "key2", "value2");
        assertEquals("OK", msetResult);

        // MGET
        List<String> values = client.mget("key1", "key2");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test
    public void testHashOperations() throws IOException {
        // HSET
        long hsetResult = client.hset("user:1", "name", "Alice");
        assertEquals(1, hsetResult);

        client.hset("user:1", "age", "25");
        client.hset("user:1", "city", "Beijing");

        // HGET
        String name = client.hget("user:1", "name");
        assertEquals("Alice", name);

        // HGETALL
        List<String> allFields = client.hgetall("user:1");
        assertTrue(allFields.contains("name"));
        assertTrue(allFields.contains("Alice"));
        assertTrue(allFields.contains("age"));
        assertTrue(allFields.contains("25"));

        // HEXISTS
        long existsResult = client.hexists("user:1", "name");
        assertEquals(1, existsResult);

        // HDEL
        long hdelResult = client.hdel("user:1", "age");
        assertEquals(1, hdelResult);
    }

    @Test
    public void testListOperations() throws IOException {
        // LPUSH
        long lpushResult = client.lpush("mylist", "item1", "item2", "item3");
        assertEquals(3, lpushResult);

        // LRANGE
        List<String> items = client.lrange("mylist", 0, -1);
        assertEquals(3, items.size());
        assertEquals("item3", items.get(0)); // LPUSH 是倒序的
        assertEquals("item1", items.get(2));

        // LPOP
        String lpopResult = client.lpop("mylist");
        assertEquals("item3", lpopResult);

        // RPUSH
        long rpushResult = client.rpush("mylist", "item4");
        assertEquals(3, rpushResult);
    }

    @Test
    public void testSetOperations() throws IOException {
        // SADD
        long saddResult = client.sadd("myset", "apple", "banana", "orange");
        assertEquals(3, saddResult);

        // SMEMBERS
        List<String> members = client.smembers("myset");
        assertEquals(3, members.size());
        assertTrue(members.contains("apple"));
        assertTrue(members.contains("banana"));

        // SISMEMBER
        long isMemberResult = client.sismember("myset", "apple");
        assertEquals(1, isMemberResult);

        long notMemberResult = client.sismember("myset", "grape");
        assertEquals(0, notMemberResult);

        // SCARD
        long scardResult = client.scard("myset");
        assertEquals(3, scardResult);

        // SREM
        long sremResult = client.srem("myset", "banana");
        assertEquals(1, sremResult);
    }

    @Test
    public void testSortedSetOperations() throws IOException {
        // ZADD
        long zaddResult1 = client.zadd("leaderboard", 100, "Player1");
        assertEquals(1, zaddResult1);

        long zaddResult2 = client.zadd("leaderboard", 250, "Player2");
        assertEquals(1, zaddResult2);

        long zaddResult3 = client.zadd("leaderboard", 150, "Player3");
        assertEquals(1, zaddResult3);

        // ZRANGE
        List<String> players = client.zrange("leaderboard", 0, -1);
        assertEquals(3, players.size());
        // 按分数排序
        assertEquals("Player1", players.get(0));
        assertEquals("Player3", players.get(1));
        assertEquals("Player2", players.get(2));

        // ZSCORE
        String score = client.zscore("leaderboard", "Player2");
        assertEquals("250", score);

        // ZCARD
        long zcardResult = client.zcard("leaderboard");
        assertEquals(3, zcardResult);

        // ZREM
        long zremResult = client.zrem("leaderboard", "Player1");
        assertEquals(1, zremResult);
    }

    @Test
    public void testKeyOperations() throws IOException {
        // 创建一些键
        client.set("string_key", "value");
        client.lpush("list_key", "item");
        client.hset("hash_key", "field", "value");

        // EXISTS
        long existsResult = client.exists("string_key", "list_key", "nonexistent");
        assertEquals(2, existsResult);

        // TYPE
        String stringType = client.type("string_key");
        assertEquals("string", stringType);

        String listType = client.type("list_key");
        assertEquals("list", listType);

        String hashType = client.type("hash_key");
        assertEquals("hash", hashType);

        // KEYS
        List<String> keys = client.keys("*_key");
        assertEquals(3, keys.size());
        assertTrue(keys.contains("string_key"));
        assertTrue(keys.contains("list_key"));
        assertTrue(keys.contains("hash_key"));
    }

    @Test
    public void testServerOperations() throws IOException {
        // 先清空数据库
        client.flushdb();
        
        // DBSIZE
        long dbSizeBefore = client.dbsize();
        
        // 添加一些数据
        client.set("test1", "value1");
        client.set("test2", "value2");
        
        long dbSizeAfter = client.dbsize();
        assertEquals(dbSizeBefore + 2, dbSizeAfter);
    }

    @Test
    public void testDelAndStrlen() throws IOException {
        client.set("to_delete", "value");
        
        // STRLEN
        long strlen = client.strlen("to_delete");
        assertEquals(5, strlen);

        // DEL
        long delResult = client.del("to_delete");
        assertEquals(1, delResult);

        // 验证已删除
        String deletedValue = client.get("to_delete");
        assertNull(deletedValue);
    }

    @Test
    public void testConnection() throws IOException {
        assertTrue(client.isConnected());
        
        client.close();
        assertFalse(client.isConnected());
        
        // 重新连接
        client.connect();
        assertTrue(client.isConnected());
    }
}
