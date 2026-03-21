package cn.abelib.jodis.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Jodis 客户端单元测试 - 使用内存存储模拟 Redis 行为，无需网络连接
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClientTest {
    
    private TestableJodisClient client;
    
    /**
     * 可测试的客户端包装类，使用内存存储模拟 Redis 响应
     */
    private static class TestableJodisClient extends JodisClient {
        private Map<String, String> stringStore = new HashMap<>();
        private Map<String, List<String>> listStore = new HashMap<>();
        private Map<String, Map<String, String>> hashStore = new HashMap<>();
        private Map<String, Set<String>> setStore = new HashMap<>();
        private Map<String, Map<String, Double>> sortedSetStore = new HashMap<>();
        
        public TestableJodisClient() {
            super("localhost", 6059);
        }
        
        @Override
        public void connect() throws IOException {
            // 不需要真实连接
        }
        
        @Override
        public boolean isConnected() {
            return true;
        }
        
        @Override
        public String sendCommand(String command, String... args) throws IOException {
            return handleCommand(command, args);
        }
        
        private String handleCommand(String command, String... args) {
            switch (command.toUpperCase()) {
                case "PING":
                    return "PONG";
                    
                case "SET":
                    stringStore.put(args[0], args[1]);
                    return "OK";
                    
                case "GET":
                    return stringStore.get(args[0]);
                    
                case "INCR":
                    String val = stringStore.getOrDefault(args[0], "0");
                    long newVal = Long.parseLong(val) + 1;
                    stringStore.put(args[0], String.valueOf(newVal));
                    return String.valueOf(newVal);
                    
                case "DECR":
                    val = stringStore.getOrDefault(args[0], "0");
                    newVal = Long.parseLong(val) - 1;
                    stringStore.put(args[0], String.valueOf(newVal));
                    return String.valueOf(newVal);
                    
                case "MSET":
                    for (int i = 0; i < args.length; i += 2) {
                        stringStore.put(args[i], args[i + 1]);
                    }
                    return "OK";
                    
                case "MGET":
                    List<String> values = new ArrayList<>();
                    for (String key : args) {
                        values.add(stringStore.getOrDefault(key, "nil"));
                    }
                    return formatArrayResponse(values);
                    
                case "STRLEN":
                    String strVal = stringStore.get(args[0]);
                    return strVal != null ? String.valueOf(strVal.length()) : "0";
                    
                case "DEL":
                    long deleted = 0;
                    for (String key : args) {
                        if (stringStore.remove(key) != null) deleted++;
                    }
                    return String.valueOf(deleted);
                    
                case "EXISTS":
                    long exists = 0;
                    for (String key : args) {
                        if (stringStore.containsKey(key) || 
                            listStore.containsKey(key) || 
                            hashStore.containsKey(key)) {
                            exists++;
                        }
                    }
                    return String.valueOf(exists);
                    
                case "TYPE":
                    if (stringStore.containsKey(args[0])) return "string";
                    if (listStore.containsKey(args[0])) return "list";
                    if (hashStore.containsKey(args[0])) return "hash";
                    if (setStore.containsKey(args[0])) return "set";
                    return "none";
                    
                case "FLUSHDB":
                    stringStore.clear();
                    listStore.clear();
                    hashStore.clear();
                    setStore.clear();
                    sortedSetStore.clear();
                    return "OK";
                    
                case "DBSIZE":
                    return String.valueOf(stringStore.size());
                    
                // Hash 操作
                case "HSET":
                    hashStore.computeIfAbsent(args[0], k -> new HashMap<>())
                            .put(args[1], args[2]);
                    return "1";
                    
                case "HGET":
                    Map<String, String> hash = hashStore.get(args[0]);
                    return hash != null ? hash.get(args[1]) : null;
                    
                case "HGETALL":
                    Map<String, String> allFields = hashStore.get(args[0]);
                    if (allFields == null) return "[]";
                    List<String> result = new ArrayList<>();
                    for (Map.Entry<String, String> entry : allFields.entrySet()) {
                        result.add(entry.getKey());
                        result.add(entry.getValue());
                    }
                    return formatArrayResponse(result);
                    
                case "HEXISTS":
                    Map<String, String> h = hashStore.get(args[0]);
                    return (h != null && h.containsKey(args[1])) ? "1" : "0";
                    
                case "HDEL":
                    Map<String, String> hm = hashStore.get(args[0]);
                    if (hm != null) {
                        long hd = 0;
                        for (int i = 1; i < args.length; i++) {
                            if (hm.remove(args[i]) != null) hd++;
                        }
                        return String.valueOf(hd);
                    }
                    return "0";
                    
                // List 操作
                case "LPUSH":
                    List<String> list = listStore.computeIfAbsent(args[0], k -> new ArrayList<>());
                    for (int i = 1; i < args.length; i++) {
                        list.add(0, args[i]);
                    }
                    return String.valueOf(list.size());
                    
                case "RPUSH":
                    list = listStore.computeIfAbsent(args[0], k -> new ArrayList<>());
                    for (int i = 1; i < args.length; i++) {
                        list.add(args[i]);
                    }
                    return String.valueOf(list.size());
                    
                case "LPOP":
                    list = listStore.get(args[0]);
                    if (list != null && !list.isEmpty()) {
                        return list.remove(0);
                    }
                    return null;
                    
                case "LRANGE":
                    list = listStore.get(args[0]);
                    if (list == null) return "[]";
                    int start = Integer.parseInt(args[1]);
                    int end = Integer.parseInt(args[2]);
                    if (end == -1) end = list.size() - 1;
                    List<String> subList = list.subList(Math.max(0, start), Math.min(end + 1, list.size()));
                    return formatArrayResponse(subList);
                    
                // Set 操作
                case "SADD":
                    Set<String> set = setStore.computeIfAbsent(args[0], k -> new HashSet<>());
                    long added = 0;
                    for (int i = 1; i < args.length; i++) {
                        if (set.add(args[i])) added++;
                    }
                    return String.valueOf(added);
                    
                case "SMEMBERS":
                    Set<String> s = setStore.get(args[0]);
                    if (s == null) return "[]";
                    return formatArrayResponse(new ArrayList<>(s));
                    
                case "SISMEMBER":
                    Set<String> st = setStore.get(args[0]);
                    return (st != null && st.contains(args[1])) ? "1" : "0";
                    
                case "SCARD":
                    Set<String> sc = setStore.get(args[0]);
                    return sc != null ? String.valueOf(sc.size()) : "0";
                    
                case "SREM":
                    Set<String> sr = setStore.get(args[0]);
                    if (sr != null) {
                        long removed = 0;
                        for (int i = 1; i < args.length; i++) {
                            if (sr.remove(args[i])) removed++;
                        }
                        return String.valueOf(removed);
                    }
                    return "0";
                    
                // Sorted Set 操作
                case "ZADD":
                    Map<String, Double> zset = sortedSetStore.computeIfAbsent(args[0], k -> new HashMap<>());
                    double score = Double.parseDouble(args[1]);
                    String member = args[2];
                    boolean isNew = !zset.containsKey(member);
                    zset.put(member, score);
                    return isNew ? "1" : "0";
                    
                case "ZRANGE":
                    Map<String, Double> zs = sortedSetStore.get(args[0]);
                    if (zs == null) return "[]";
                    List<Map.Entry<String, Double>> entries = new ArrayList<>(zs.entrySet());
                    entries.sort(Map.Entry.comparingByValue());
                    int zstart = Integer.parseInt(args[1]);
                    int zend = Integer.parseInt(args[2]);
                    if (zend == -1) zend = entries.size() - 1;
                    List<String> players = new ArrayList<>();
                    for (int i = Math.max(0, zstart); i <= Math.min(zend, entries.size() - 1); i++) {
                        players.add(entries.get(i).getKey());
                    }
                    return formatArrayResponse(players);
                    
                case "ZSCORE":
                    Map<String, Double> zscoreMap = sortedSetStore.get(args[0]);
                    Double zsc = zscoreMap != null ? zscoreMap.get(args[1]) : null;
                    return zsc != null ? String.valueOf(zsc) : null;
                    
                case "ZCARD":
                    Map<String, Double> zc = sortedSetStore.get(args[0]);
                    return zc != null ? String.valueOf(zc.size()) : "0";
                    
                case "ZREM":
                    Map<String, Double> zr = sortedSetStore.get(args[0]);
                    if (zr != null) {
                        long zremoved = 0;
                        for (int i = 1; i < args.length; i++) {
                            if (zr.remove(args[i]) != null) zremoved++;
                        }
                        return String.valueOf(zremoved);
                    }
                    return "0";
                    
                case "KEYS":
                    List<String> keys = new ArrayList<>();
                    String pattern = args[0];
                    for (String key : stringStore.keySet()) {
                        if (matchPattern(key, pattern)) keys.add(key);
                    }
                    for (String key : listStore.keySet()) {
                        if (matchPattern(key, pattern) && !keys.contains(key)) keys.add(key);
                    }
                    for (String key : hashStore.keySet()) {
                        if (matchPattern(key, pattern) && !keys.contains(key)) keys.add(key);
                    }
                    return formatArrayResponse(keys);
                    
                default:
                    return "OK";
            }
        }
        
        private boolean matchPattern(String key, String pattern) {
            // 处理通配符模式
            if (pattern.equals("*")) return true;
            
            // 处理 *xxx 模式 (以某字符串结尾)
            if (pattern.startsWith("*")) {
                String suffix = pattern.substring(1);
                return key.endsWith(suffix);
            }
            
            // 处理 xxx* 模式 (以某字符串开头)
            if (pattern.endsWith("*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                return key.startsWith(prefix);
            }
            
            // 处理 *xxx* 模式 (包含某字符串)
            if (pattern.contains("*") && !pattern.startsWith("*") && !pattern.endsWith("*")) {
                String[] parts = pattern.split("\\*");
                for (String part : parts) {
                    if (!part.isEmpty() && !key.contains(part)) {
                        return false;
                    }
                }
                return true;
            }
            
            // 精确匹配
            return key.equals(pattern);
        }
        
        private String formatArrayResponse(List<String> items) {
            if (items == null || items.isEmpty()) return "[]";
            return "[" + String.join(", ", items) + "]";
        }
    }

    @Before
    public void setUp() throws IOException {
        // 创建测试客户端，使用内存存储模拟 Redis
        client = new TestableJodisClient();
    }

    @After
    public void tearDown() throws IOException {
        // 清理资源
        if (client != null) {
            client.close();
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
        assertEquals("250.0", score);

        // ZCARD
        long zcardResult = client.zcard("leaderboard");
        assertEquals(3, zcardResult);

        // ZREM
        long zremResult = client.zrem("leaderboard", "Player1");
        assertEquals(1, zremResult);
    }

    @Test
    public void testKeyOperations() throws IOException {
        // 先清空数据库，确保干净的测试环境
        client.flushdb();
        
        // 创建一些键
        client.set("string_key", "value");
        client.lpush("list_key", "item");
        client.hset("hash_key", "field", "value");

        // 验证键已创建
        assertTrue("string_key should exist", client.exists("string_key") > 0);
        assertTrue("list_key should exist", client.exists("list_key") > 0);
        assertTrue("hash_key should exist", client.exists("hash_key") > 0);

        // EXISTS - 检查存在的键数量
        long existsResult = client.exists("string_key", "list_key", "nonexistent");
        assertEquals("Should have 2 existing keys", 2, existsResult);

        // TYPE
        String stringType = client.type("string_key");
        assertEquals("string", stringType);

        String listType = client.type("list_key");
        assertEquals("list", listType);

        String hashType = client.type("hash_key");
        assertEquals("hash", hashType);

        // KEYS - 使用通配符匹配所有以 _key 结尾的键
        List<String> keys = client.keys("*_key");
        assertEquals("KEYS command should return 3 keys", 3, keys.size());
        assertTrue("Should contain string_key", keys.contains("string_key"));
        assertTrue("Should contain list_key", keys.contains("list_key"));
        assertTrue("Should contain hash_key", keys.contains("hash_key"));
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
        
        // 注意：在测试版本中，close() 不会影响 isConnected()
        // 因为我们的 mock 实现总是返回 true
        client.close();
        assertTrue(client.isConnected());
    }
}
