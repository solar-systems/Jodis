package cn.abelib.jodis.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Jodis 客户端 - 高级 API 封装
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClient implements Closeable {
    private JodisConnection connection;
    private JodisClientConfig config;

    public JodisClient(JodisClientConfig config) {
        this.config = config;
    }

    public JodisClient(String host, int port) {
        this.config = new JodisClientConfig(host, port);
    }

    /**
     * 连接到服务器
     * @throws IOException
     */
    public void connect() throws IOException {
        if (connection == null || !connection.isConnected()) {
            connection = new JodisConnection(config.getHost(), config.getPort());
            connection.connect();
        }
    }

    /**
     * 检查连接状态
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * PING 命令 - 测试连接
     * @return "PONG" 或错误信息
     * @throws IOException
     */
    public String ping() throws IOException {
        return sendCommand("PING");
    }

    // ==================== String 操作 ====================

    /**
     * SET 命令
     * @param key 键
     * @param value 值
     * @return "OK"
     * @throws IOException
     */
    public String set(String key, String value) throws IOException {
        return sendCommand("SET", key, value);
    }

    /**
     * GET 命令
     * @param key 键
     * @return 值，不存在返回 null
     * @throws IOException
     */
    public String get(String key) throws IOException {
        return sendCommand("GET", key);
    }

    /**
     * GETSET 命令
     * @param key 键
     * @param value 新值
     * @return 旧值
     * @throws IOException
     */
    public String getSet(String key, String value) throws IOException {
        return sendCommand("GETSET", key, value);
    }

    /**
     * MSET 命令 - 批量设置
     * @param keysValues 键值对数组 [key1, value1, key2, value2, ...]
     * @return "OK"
     * @throws IOException
     */
    public String mset(String... keysValues) throws IOException {
        List<String> args = new ArrayList<>();
        for (String kv : keysValues) {
            args.add(kv);
        }
        return sendCommand("MSET", args.toArray(new String[0]));
    }

    /**
     * MGET 命令 - 批量获取
     * @param keys 键数组
     * @return 值列表
     * @throws IOException
     */
    public List<String> mget(String... keys) throws IOException {
        String response = sendCommand("MGET", keys);
        return parseArrayResponse(response);
    }

    /**
     * INCR 命令 - 自增
     * @param key 键
     * @return 自增后的值
     * @throws IOException
     */
    public long incr(String key) throws IOException {
        String response = sendCommand("INCR", key);
        return Long.parseLong(response);
    }

    /**
     * DECR 命令 - 自减
     * @param key 键
     * @return 自减后的值
     * @throws IOException
     */
    public long decr(String key) throws IOException {
        String response = sendCommand("DECR", key);
        return Long.parseLong(response);
    }

    /**
     * STRLEN 命令 - 获取字符串长度
     * @param key 键
     * @return 长度
     * @throws IOException
     */
    public long strlen(String key) throws IOException {
        String response = sendCommand("STRLEN", key);
        return Long.parseLong(response);
    }
    
    /**
     * Redis command: EXPIRE
     * @param key key
     * @param seconds 过期时间（秒）
     * @return 1 成功，0 失败
     */
    public long expire(String key, int seconds) throws IOException {
        String response = sendCommand("EXPIRE", key, String.valueOf(seconds));
        return Long.parseLong(response);
    }
    
    /**
     * Redis command: TTL
     * @param key key
     * @return 剩余秒数；-1 永不过期；-2 key不存在
     */
    public long ttl(String key) throws IOException {
        String response = sendCommand("TTL", key);
        return Long.parseLong(response);
    }
    
    /**
     * Redis command: SETEX
     * @param key key
     * @param seconds 过期时间（秒）
     * @param value 值
     * @return OK
     */
    public String setex(String key, int seconds, String value) throws IOException {
        return sendCommand("SETEX", key, String.valueOf(seconds), value);
    }

    /**
     * DEL 命令 - 删除键
     * @param keys 键数组
     * @return 删除的数量
     * @throws IOException
     */
    public long del(String... keys) throws IOException {
        long count = 0;
        for (String key : keys) {
            String response = sendCommand("DEL", key);
            count += Long.parseLong(response);
        }
        return count;
    }

    /**
     * EXISTS 命令 - 检查键是否存在
     * @param keys 键数组
     * @return 存在的数量
     * @throws IOException
     */
    public long exists(String... keys) throws IOException {
        long count = 0;
        for (String key : keys) {
            String response = sendCommand("EXISTS", key);
            count += Long.parseLong(response);
        }
        return count;
    }

    /**
     * TYPE 命令 - 获取键的类型
     * @param key 键
     * @return 类型：string, list, hash, set, zset, none
     * @throws IOException
     */
    public String type(String key) throws IOException {
        return sendCommand("TYPE", key);
    }

    // ==================== Hash 操作 ====================

    /**
     * HSET 命令
     * @param key 哈希表名
     * @param field 字段
     * @param value 值
     * @return 1 表示新字段，0 表示修改现有字段
     * @throws IOException
     */
    public long hset(String key, String field, String value) throws IOException {
        String response = sendCommand("HSET", key, field, value);
        return Long.parseLong(response);
    }

    /**
     * HMSET 命令 - 批量设置哈希表字段
     * @param key 哈希表名
     * @param fieldsValues 字段值对数组
     * @return "OK"
     * @throws IOException
     */
    public String hmset(String key, String... fieldsValues) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String fv : fieldsValues) {
            args.add(fv);
        }
        return sendCommand("HMSET", args.toArray(new String[0]));
    }

    /**
     * HGET 命令
     * @param key 哈希表名
     * @param field 字段
     * @return 值
     * @throws IOException
     */
    public String hget(String key, String field) throws IOException {
        return sendCommand("HGET", key, field);
    }

    /**
     * HMGET 命令 - 批量获取哈希表字段
     * @param key 哈希表名
     * @param fields 字段数组
     * @return 值列表
     * @throws IOException
     */
    public List<String> hmget(String key, String... fields) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String field : fields) {
            args.add(field);
        }
        String response = sendCommand("HMGET", args.toArray(new String[0]));
        return parseArrayResponse(response);
    }

    /**
     * HGETALL 命令 - 获取所有字段和值
     * @param key 哈希表名
     * @return 字段和值的交替列表
     * @throws IOException
     */
    public List<String> hgetall(String key) throws IOException {
        String response = sendCommand("HGETALL", key);
        return parseArrayResponse(response);
    }

    /**
     * HDEL 命令 - 删除哈希表字段
     * @param key 哈希表名
     * @param fields 字段数组
     * @return 删除的数量
     * @throws IOException
     */
    public long hdel(String key, String... fields) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String field : fields) {
            args.add(field);
        }
        String response = sendCommand("HDEL", args.toArray(new String[0]));
        return Long.parseLong(response);
    }

    /**
     * HEXISTS 命令 - 检查字段是否存在
     * @param key 哈希表名
     * @param field 字段
     * @return 1 存在，0 不存在
     * @throws IOException
     */
    public long hexists(String key, String field) throws IOException {
        String response = sendCommand("HEXISTS", key, field);
        return Long.parseLong(response);
    }

    /**
     * HKEYS 命令 - 获取所有字段名
     * @param key 哈希表名
     * @return 字段名列表
     * @throws IOException
     */
    public List<String> hkeys(String key) throws IOException {
        String response = sendCommand("HKEYS", key);
        return parseArrayResponse(response);
    }

    /**
     * HVALS 命令 - 获取所有值
     * @param key 哈希表名
     * @return 值列表
     * @throws IOException
     */
    public List<String> hvals(String key) throws IOException {
        String response = sendCommand("HVALS", key);
        return parseArrayResponse(response);
    }

    // ==================== List 操作 ====================

    /**
     * LPUSH 命令 - 从左侧插入列表
     * @param key 列表名
     * @param values 值数组
     * @return 列表长度
     * @throws IOException
     */
    public long lpush(String key, String... values) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String value : values) {
            args.add(value);
        }
        String response = sendCommand("LPUSH", args.toArray(new String[0]));
        return Long.parseLong(response);
    }

    /**
     * RPUSH 命令 - 从右侧插入列表
     * @param key 列表名
     * @param values 值数组
     * @return 列表长度
     * @throws IOException
     */
    public long rpush(String key, String... values) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String value : values) {
            args.add(value);
        }
        String response = sendCommand("RPUSH", args.toArray(new String[0]));
        return Long.parseLong(response);
    }

    /**
     * LPOP 命令 - 从左侧弹出元素
     * @param key 列表名
     * @return 弹出的值
     * @throws IOException
     */
    public String lpop(String key) throws IOException {
        return sendCommand("LPOP", key);
    }

    /**
     * RPOP 命令 - 从右侧弹出元素
     * @param key 列表名
     * @return 弹出的值
     * @throws IOException
     */
    public String rpop(String key) throws IOException {
        return sendCommand("RPOP", key);
    }

    /**
     * LRANGE 命令 - 获取列表指定范围的元素
     * @param key 列表名
     * @param start 起始索引（包含）
     * @param end 结束索引（包含）
     * @return 元素列表
     * @throws IOException
     */
    public List<String> lrange(String key, long start, long end) throws IOException {
        String response = sendCommand("LRANGE", key, String.valueOf(start), String.valueOf(end));
        return parseArrayResponse(response);
    }

    /**
     * LINDEX 命令 - 获取列表指定索引的元素
     * @param key 列表名
     * @param index 索引
     * @return 元素值
     * @throws IOException
     */
    public String lindex(String key, long index) throws IOException {
        return sendCommand("LINDEX", key, String.valueOf(index));
    }

    // ==================== Set 操作 ====================

    /**
     * SADD 命令 - 添加集合成员
     * @param key 集合名
     * @param members 成员数组
     * @return 成功添加的数量
     * @throws IOException
     */
    public long sadd(String key, String... members) throws IOException {
        long count = 0;
        for (String member : members) {
            String response = sendCommand("SADD", key, member);
            count += Long.parseLong(response);
        }
        return count;
    }

    /**
     * SMEMBERS 命令 - 获取所有成员
     * @param key 集合名
     * @return 成员列表
     * @throws IOException
     */
    public List<String> smembers(String key) throws IOException {
        String response = sendCommand("SMEMBERS", key);
        return parseArrayResponse(response);
    }

    /**
     * SISMEMBER 命令 - 检查成员是否存在
     * @param key 集合名
     * @param member 成员
     * @return 1 存在，0 不存在
     * @throws IOException
     */
    public long sismember(String key, String member) throws IOException {
        String response = sendCommand("SISMEMBER", key, member);
        return Long.parseLong(response);
    }

    /**
     * SREM 命令 - 删除成员
     * @param key 集合名
     * @param members 成员数组
     * @return 删除的数量
     * @throws IOException
     */
    public long srem(String key, String... members) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(key);
        for (String member : members) {
            args.add(member);
        }
        String response = sendCommand("SREM", args.toArray(new String[0]));
        return Long.parseLong(response);
    }

    /**
     * SCARD 命令 - 获取集合大小
     * @param key 集合名
     * @return 集合大小
     * @throws IOException
     */
    public long scard(String key) throws IOException {
        String response = sendCommand("SCARD", key);
        return Long.parseLong(response);
    }

    // ==================== Sorted Set 操作 ====================

    /**
     * ZADD 命令 - 添加有序集合成员
     * @param key 有序集合名
     * @param score 分数
     * @param member 成员
     * @return 成功添加的数量
     * @throws IOException
     */
    public long zadd(String key, double score, String member) throws IOException {
        String response = sendCommand("ZADD", key, String.valueOf(score), member);
        return Long.parseLong(response);
    }

    /**
     * ZRANGE 命令 - 获取指定排名范围的成员
     * @param key 有序集合名
     * @param start 起始排名
     * @param end 结束排名
     * @return 成员列表
     * @throws IOException
     */
    public List<String> zrange(String key, long start, long end) throws IOException {
        String response = sendCommand("ZRANGE", key, String.valueOf(start), String.valueOf(end));
        return parseArrayResponse(response);
    }

    /**
     * ZSCORE 命令 - 获取成员的分数
     * @param key 有序集合名
     * @param member 成员
     * @return 分数
     * @throws IOException
     */
    public String zscore(String key, String member) throws IOException {
        return sendCommand("ZSCORE", key, member);
    }

    /**
     * ZREM 命令 - 删除成员
     * @param key 有序集合名
     * @param members 成员数组
     * @return 删除的数量
     * @throws IOException
     */
    public long zrem(String key, String... members) throws IOException {
        long count = 0;
        for (String member : members) {
            String response = sendCommand("ZREM", key, member);
            count += Long.parseLong(response);
        }
        return count;
    }

    /**
     * ZCARD 命令 - 获取有序集合大小
     * @param key 有序集合名
     * @return 集合大小
     * @throws IOException
     */
    public long zcard(String key) throws IOException {
        String response = sendCommand("ZCARD", key);
        return Long.parseLong(response);
    }

    // ==================== Server 操作 ====================

    /**
     * FLUSHDB 命令 - 清空数据库
     * @return "OK"
     * @throws IOException
     */
    public String flushdb() throws IOException {
        return sendCommand("FLUSHDB");
    }

    /**
     * DBSIZE 命令 - 获取数据库大小
     * @return 键的数量
     * @throws IOException
     */
    public long dbsize() throws IOException {
        String response = sendCommand("DBSIZE");
        return Long.parseLong(response);
    }

    /**
     * KEYS 命令 - 获取匹配的所有键
     * @param pattern 模式（如 *）
     * @return 键列表
     * @throws IOException
     */
    public List<String> keys(String pattern) throws IOException {
        String response = sendCommand("KEYS", pattern);
        return parseArrayResponse(response);
    }

    /**
     * SCAN 命令 - 增量迭代数据库中的键
     * @param cursor 游标，从 "0" 开始
     * @return [nextCursor, key1, key2, ...]，当 nextCursor 为 "0" 时表示迭代结束
     * @throws IOException
     */
    public List<String> scan(String cursor) throws IOException {
        String response = sendCommand("SCAN", cursor);
        return parseArrayResponse(response);
    }

    /**
     * SCAN 命令 - 增量迭代数据库中的键（带 MATCH 选项）
     * @param cursor 游标，从 "0" 开始
     * @param pattern 匹配模式（如 "user:*"）
     * @return [nextCursor, key1, key2, ...]
     * @throws IOException
     */
    public List<String> scan(String cursor, String pattern) throws IOException {
        String response = sendCommand("SCAN", cursor, "MATCH", pattern);
        return parseArrayResponse(response);
    }

    /**
     * SCAN 命令 - 增量迭代数据库中的键（带 MATCH 和 COUNT 选项）
     * @param cursor 游标，从 "0" 开始
     * @param pattern 匹配模式（如 "user:*"）
     * @param count 每次返回的元素数量建议值
     * @return [nextCursor, key1, key2, ...]
     * @throws IOException
     */
    public List<String> scan(String cursor, String pattern, int count) throws IOException {
        String response = sendCommand("SCAN", cursor, "MATCH", pattern, "COUNT", String.valueOf(count));
        return parseArrayResponse(response);
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送原始命令
     * @param command 命令
     * @param args 参数
     * @return 响应字符串
     * @throws IOException
     */
    public String sendCommand(String command, String... args) throws IOException {
        if (!isConnected()) {
            connect();
        }
        return connection.sendCommand(command, args);
    }

    /**
     * 解析数组响应
     * @param response 响应字符串
     * @return 字符串列表
     */
    private List<String> parseArrayResponse(String response) {
        if (response == null || response.equals("null") || response.equals("nil")) {
            return new ArrayList<>();
        }
        
        // 处理格式：[value1, value2, ...]
        List<String> result = new ArrayList<>();
        if (response.startsWith("[") && response.endsWith("]")) {
            String content = response.substring(1, response.length() - 1).trim();
            if (!content.isEmpty()) {
                String[] items = content.split(", ");
                for (String item : items) {
                    result.add(item.trim());
                }
            }
        }
        return result;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }
}
