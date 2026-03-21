package cn.abelib.jodis.protocol;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @Author: abel.huang
 * @Date: 2020-07-15 00:26
 */
public class ReqCmdTest {

    /**
     * 测试 Server 命令
     */
    @Test
    public void testServerCommands() {
        // PING
        Request ping = new Request("PING", Arrays.asList());
        assertEquals("PING", ping.getCommand());
        assertFalse(ping.isError());

        // FLUSHDB - 不需要写入日志，会生成快照
        Request flushdb = new Request("FLUSHDB", Arrays.asList());
        assertEquals("FLUSHDB", flushdb.getCommand());
        assertFalse(flushdb.needLog());

        // DBSIZE
        Request dbsize = new Request("DBSIZE", Arrays.asList());
        assertEquals("DBSIZE", dbsize.getCommand());
    }

    /**
     * 测试 Key 操作命令
     */
    @Test
    public void testKeyCommands() {
        // DEL
        Request del = new Request("DEL", Arrays.asList("key1", "key2"));
        assertEquals("DEL", del.getCommand());
        assertTrue(del.needLog());
        assertEquals(2, del.getArgs().size());

        // EXISTS
        Request exists = new Request("EXISTS", Arrays.asList("key1"));
        assertEquals("EXISTS", exists.getCommand());
        assertEquals(1, exists.getArgs().size());

        // TYPE
        Request type = new Request("TYPE", Arrays.asList("key1"));
        assertEquals("TYPE", type.getCommand());

        // EXPIRE
        Request expire = new Request("EXPIRE", Arrays.asList("key1", "60"));
        assertEquals("EXPIRE", expire.getCommand());
        assertEquals(2, expire.getArgs().size());

        // TTL
        Request ttl = new Request("TTL", Arrays.asList("key1"));
        assertEquals("TTL", ttl.getCommand());

        // KEYS
        Request keys = new Request("KEYS", Arrays.asList("pattern*"));
        assertEquals("KEYS", keys.getCommand());

        // RENAME
        Request rename = new Request("RENAME", Arrays.asList("oldkey", "newkey"));
        assertEquals("RENAME", rename.getCommand());
        assertTrue(rename.needLog());

        // RENAMENX
        Request renamenx = new Request("RENAMENX", Arrays.asList("oldkey", "newkey"));
        assertEquals("RENAMENX", renamenx.getCommand());
        assertTrue(renamenx.needLog());
    }

    /**
     * 测试 String 操作命令
     */
    @Test
    public void testStringCommands() {
        // SET
        Request set = new Request("SET", Arrays.asList("key", "value"));
        assertEquals("SET", set.getCommand());
        assertTrue(set.needLog());
        assertEquals(2, set.getArgs().size());

        // GET
        Request get = new Request("GET", Arrays.asList("key"));
        assertEquals("GET", get.getCommand());
        assertEquals(1, get.getArgs().size());

        // GETSET
        Request getset = new Request("GETSET", Arrays.asList("key", "newvalue"));
        assertEquals("GETSET", getset.getCommand());
        assertTrue(getset.needLog());

        // MGET
        Request mget = new Request("MGET", Arrays.asList("key1", "key2", "key3"));
        assertEquals("MGET", mget.getCommand());
        assertEquals(3, mget.getArgs().size());

        // MSET
        Request mset = new Request("MSET", Arrays.asList("key1", "value1", "key2", "value2"));
        assertEquals("MSET", mset.getCommand());
        assertTrue(mset.needLog());

        // SETEX
        Request setex = new Request("SETEX", Arrays.asList("key", "60", "value"));
        assertEquals("SETEX", setex.getCommand());
        assertTrue(setex.needLog());

        // SETNX
        Request setnx = new Request("SETNX", Arrays.asList("key", "value"));
        assertEquals("SETNX", setnx.getCommand());
        assertTrue(setnx.needLog());

        // STRLEN
        Request strlen = new Request("STRLEN", Arrays.asList("key"));
        assertEquals("STRLEN", strlen.getCommand());

        // INCR
        Request incr = new Request("INCR", Arrays.asList("counter"));
        assertEquals("INCR", incr.getCommand());
        assertTrue(incr.needLog());

        // INCRBY
        Request incrby = new Request("INCRBY", Arrays.asList("counter", "10"));
        assertEquals("INCRBY", incrby.getCommand());
        assertTrue(incrby.needLog());

        // INCRBYFLOAT
        Request incrbyfloat = new Request("INCRBYFLOAT", Arrays.asList("counter", "1.5"));
        assertEquals("INCRBYFLOAT", incrbyfloat.getCommand());
        assertTrue(incrbyfloat.needLog());

        // DECR
        Request decr = new Request("DECR", Arrays.asList("counter"));
        assertEquals("DECR", decr.getCommand());
        assertTrue(decr.needLog());

        // DECRBY
        Request decrby = new Request("DECRBY", Arrays.asList("counter", "5"));
        assertEquals("DECRBY", decrby.getCommand());
        assertTrue(decrby.needLog());

        // APPEND
        Request append = new Request("APPEND", Arrays.asList("key", "suffix"));
        assertEquals("APPEND", append.getCommand());
        assertTrue(append.needLog());

        // GETRANGE
        Request getrange = new Request("GETRANGE", Arrays.asList("key", "0", "5"));
        assertEquals("GETRANGE", getrange.getCommand());
    }

    /**
     * 测试 Hash 操作命令
     */
    @Test
    public void testHashCommands() {
        // HSET
        Request hset = new Request("HSET", Arrays.asList("hash", "field", "value"));
        assertEquals("HSET", hset.getCommand());
        assertTrue(hset.needLog());

        // HGET
        Request hget = new Request("HGET", Arrays.asList("hash", "field"));
        assertEquals("HGET", hget.getCommand());

        // HMSET
        Request hmset = new Request("HMSET", Arrays.asList("hash", "field1", "value1", "field2", "value2"));
        assertEquals("HMSET", hmset.getCommand());
        assertTrue(hmset.needLog());

        // HMGET
        Request hmget = new Request("HMGET", Arrays.asList("hash", "field1", "field2"));
        assertEquals("HMGET", hmget.getCommand());

        // HGETALL
        Request hgetall = new Request("HGETALL", Arrays.asList("hash"));
        assertEquals("HGETALL", hgetall.getCommand());

        // HDEL
        Request hdel = new Request("HDEL", Arrays.asList("hash", "field"));
        assertEquals("HDEL", hdel.getCommand());
        assertTrue(hdel.needLog());

        // HEXISTS
        Request hexists = new Request("HEXISTS", Arrays.asList("hash", "field"));
        assertEquals("HEXISTS", hexists.getCommand());

        // HLEN
        Request hlen = new Request("HLEN", Arrays.asList("hash"));
        assertEquals("HLEN", hlen.getCommand());

        // HKEYS
        Request hkeys = new Request("HKEYS", Arrays.asList("hash"));
        assertEquals("HKEYS", hkeys.getCommand());

        // HVALS
        Request hvals = new Request("HVALS", Arrays.asList("hash"));
        assertEquals("HVALS", hvals.getCommand());

        // HINCRBY
        Request hincrby = new Request("HINCRBY", Arrays.asList("hash", "field", "10"));
        assertEquals("HINCRBY", hincrby.getCommand());
        assertTrue(hincrby.needLog());

        // HINCRBYFLOAT
        Request hincrbyfloat = new Request("HINCRBYFLOAT", Arrays.asList("hash", "field", "1.5"));
        assertEquals("HINCRBYFLOAT", hincrbyfloat.getCommand());
        assertTrue(hincrbyfloat.needLog());

        // HSETNX
        Request hsetnx = new Request("HSETNX", Arrays.asList("hash", "field", "value"));
        assertEquals("HSETNX", hsetnx.getCommand());
        assertTrue(hsetnx.needLog());
    }

    /**
     * 测试 List 操作命令
     */
    @Test
    public void testListCommands() {
        // LPUSH
        Request lpush = new Request("LPUSH", Arrays.asList("list", "value1", "value2"));
        assertEquals("LPUSH", lpush.getCommand());
        assertTrue(lpush.needLog());

        // RPUSH
        Request rpush = new Request("RPUSH", Arrays.asList("list", "value1", "value2"));
        assertEquals("RPUSH", rpush.getCommand());
        assertTrue(rpush.needLog());

        // LPOP
        Request lpop = new Request("LPOP", Arrays.asList("list"));
        assertEquals("LPOP", lpop.getCommand());
        assertTrue(lpop.needLog());

        // RPOP
        Request rpop = new Request("RPOP", Arrays.asList("list"));
        assertEquals("RPOP", rpop.getCommand());
        assertTrue(rpop.needLog());

        // LRANGE
        Request lrange = new Request("LRANGE", Arrays.asList("list", "0", "-1"));
        assertEquals("LRANGE", lrange.getCommand());

        // LINDEX
        Request lindex = new Request("LINDEX", Arrays.asList("list", "0"));
        assertEquals("LINDEX", lindex.getCommand());

        // LSET
        Request lset = new Request("LSET", Arrays.asList("list", "0", "newvalue"));
        assertEquals("LSET", lset.getCommand());

        // LINSERT
        Request linsert = new Request("LINSERT", Arrays.asList("list", "BEFORE", "pivot", "value"));
        assertEquals("LINSERT", linsert.getCommand());
        assertTrue(linsert.needLog());

        // LREM
        Request lrem = new Request("LREM", Arrays.asList("list", "2", "value"));
        assertEquals("LREM", lrem.getCommand());
        assertTrue(lrem.needLog());

        // LTRIM
        Request ltrim = new Request("LTRIM", Arrays.asList("list", "0", "10"));
        assertEquals("LTRIM", ltrim.getCommand());
        assertTrue(ltrim.needLog());
    }

    /**
     * 测试 Set 操作命令
     */
    @Test
    public void testSetCommands() {
        // SADD
        Request sadd = new Request("SADD", Arrays.asList("set", "member1", "member2"));
        assertEquals("SADD", sadd.getCommand());
        assertTrue(sadd.needLog());

        // SMEMBERS
        Request smembers = new Request("SMEMBERS", Arrays.asList("set"));
        assertEquals("SMEMBERS", smembers.getCommand());

        // SISMEMBER
        Request sismember = new Request("SISMEMBER", Arrays.asList("set", "member"));
        assertEquals("SISMEMBER", sismember.getCommand());

        // SCARD
        Request scard = new Request("SCARD", Arrays.asList("set"));
        assertEquals("SCARD", scard.getCommand());

        // SREM
        Request srem = new Request("SREM", Arrays.asList("set", "member"));
        assertEquals("SREM", srem.getCommand());
        assertTrue(srem.needLog());

        // SPOP
        Request spop = new Request("SPOP", Arrays.asList("set"));
        assertEquals("SPOP", spop.getCommand());

        // SRANDMEMBER
        Request srandmember = new Request("SRANDMEMBER", Arrays.asList("set"));
        assertEquals("SRANDMEMBER", srandmember.getCommand());

        // SMOVE
        Request smove = new Request("SMOVE", Arrays.asList("srcset", "dstset", "member"));
        assertEquals("SMOVE", smove.getCommand());
        assertTrue(smove.needLog());

        // SDIFF
        Request sdiff = new Request("SDIFF", Arrays.asList("set1", "set2"));
        assertEquals("SDIFF", sdiff.getCommand());

        // SUNION
        Request sunion = new Request("SUNION", Arrays.asList("set1", "set2"));
        assertEquals("SUNION", sunion.getCommand());

        // SINTER
        Request sinter = new Request("SINTER", Arrays.asList("set1", "set2"));
        assertEquals("SINTER", sinter.getCommand());
    }

    /**
     * 测试 SortedSet 操作命令
     */
    @Test
    public void testSortedSetCommands() {
        // ZADD
        Request zadd = new Request("ZADD", Arrays.asList("zset", "100", "member1"));
        assertEquals("ZADD", zadd.getCommand());
        assertTrue(zadd.needLog());

        // ZSCORE
        Request zscore = new Request("ZSCORE", Arrays.asList("zset", "member1"));
        assertEquals("ZSCORE", zscore.getCommand());

        // ZRANGE
        Request zrange = new Request("ZRANGE", Arrays.asList("zset", "0", "-1"));
        assertEquals("ZRANGE", zrange.getCommand());

        // ZCARD
        Request zcard = new Request("ZCARD", Arrays.asList("zset"));
        assertEquals("ZCARD", zcard.getCommand());

        // ZCOUNT
        Request zcount = new Request("ZCOUNT", Arrays.asList("zset", "0", "100"));
        assertEquals("ZCOUNT", zcount.getCommand());

        // ZREM
        Request zrem = new Request("ZREM", Arrays.asList("zset", "member1"));
        assertEquals("ZREM", zrem.getCommand());
        assertTrue(zrem.needLog());

        // ZRANK
        Request zrank = new Request("ZRANK", Arrays.asList("zset", "member1"));
        assertEquals("ZRANK", zrank.getCommand());

        // ZREVRANGE
        Request zrevrange = new Request("ZREVRANGE", Arrays.asList("zset", "0", "-1"));
        assertEquals("ZREVRANGE", zrevrange.getCommand());

        // ZREVRANK
        Request zrevrank = new Request("ZREVRANK", Arrays.asList("zset", "member1"));
        assertEquals("ZREVRANK", zrevrank.getCommand());

        // ZINCRBY
        Request zincrby = new Request("ZINCRBY", Arrays.asList("zset", "10", "member1"));
        assertEquals("ZINCRBY", zincrby.getCommand());
    }

    /**
     * 测试命令大小写不敏感
     */
    @Test
    public void testCommandCaseInsensitive() {
        Request lowerCase = new Request("set", Arrays.asList("key", "value"));
        Request upperCase = new Request("SET", Arrays.asList("key", "value"));
        Request mixedCase = new Request("SeT", Arrays.asList("key", "value"));

        assertEquals(lowerCase.getCommand(), upperCase.getCommand());
        assertEquals(upperCase.getCommand(), mixedCase.getCommand());
        assertEquals("SET", lowerCase.getCommand());
    }

    /**
     * 测试错误请求处理
     */
    @Test
    public void testBadRequest() {
        Response errorResponse = ErrorResponse.errorUnknownCmd("UNKNOWN");
        Request badRequest = Request.badRequest(errorResponse);

        assertTrue(badRequest.isError());
        assertNotNull(badRequest.errorResponse());
        assertNull(badRequest.getCommand());
    }

    /**
     * 测试 NEED_LOGS 集合包含的命令
     */
    @Test
    public void testNeedLogsCommands() {
        // 验证部分需要日志的命令
        assertTrue(ProtocolConstant.NEED_LOGS.contains("SET"));
        assertTrue(ProtocolConstant.NEED_LOGS.contains("DEL"));
        assertTrue(ProtocolConstant.NEED_LOGS.contains("LPUSH"));
        assertTrue(ProtocolConstant.NEED_LOGS.contains("SADD"));
        assertTrue(ProtocolConstant.NEED_LOGS.contains("ZADD"));
        assertTrue(ProtocolConstant.NEED_LOGS.contains("HSET"));

        // 验证不需要日志的命令
        assertFalse(ProtocolConstant.NEED_LOGS.contains("GET"));
        assertFalse(ProtocolConstant.NEED_LOGS.contains("MGET"));
        assertFalse(ProtocolConstant.NEED_LOGS.contains("LRANGE"));
    }
}
