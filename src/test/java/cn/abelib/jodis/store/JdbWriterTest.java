package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.KeyValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JdbWriter 单元测试
 * 测试各种数据类型的 ByteBuffer 序列化格式
 *
 * @Author: abel.huang
 * @Date: 2020-07-22 01:48
 */
public class JdbWriterTest {
    private JdbWriter jdbWriter;
    private JdbReader jdbReader;

    @Before
    public void init() throws Exception {
        // JdbWriter 每次会删除旧文件重新创建
        jdbWriter = new JdbWriter("log/", "test.default.jdb");
        // JdbReader 用于测试 read 方法（不初始化文件，只使用其解析方法）
        jdbReader = new JdbReader();
    }

    @Test
    public void writeStringTest() throws Exception {
        String key = "hello";
        JodisObject obj = new JodisObject(new JodisString("world"), KeyType.JODIS_STRING, EncodingType.OBJ_ENCODING_RAW.getType());
        obj.setExpireTime(0);  // 永不过期

        // 获取序列化后的 ByteBuffer
        ByteBuffer buffer = jdbWriter.writeString(key, obj);

        // 跳过 type(1) + totalLen(4) = 5 字节
        ByteBuffer sub = ByteBuffer.allocate(buffer.capacity() - 5);
        buffer.position(5);
        sub.put(buffer);
        sub.flip();  // 切换到读取模式

        // 使用 JdbReader 解析
        KeyValue<String, JodisObject> keyValue = jdbReader.readString(sub);
        Assert.assertEquals(key, keyValue.getKey());
        Assert.assertEquals("world", ((JodisString)keyValue.getValue().getValue()).getHolder());
    }

    @Test
    public void writeListTest() throws Exception {
        String key = "hello";
        List<String> list = Lists.newArrayList("hello", "world");
        JodisObject obj = new JodisObject(new JodisList(list), KeyType.JODIS_LIST, EncodingType.OBJ_ENCODING_LIST.getType());
        obj.setExpireTime(0);

        ByteBuffer buf = jdbWriter.writeList(key, obj);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        sub.flip();  // 切换到读取模式

        KeyValue<String, JodisObject> keyValue = jdbReader.readList(sub);
        Assert.assertEquals(key, keyValue.getKey());
        Assert.assertEquals(2, ((JodisList)keyValue.getValue().getValue()).getHolder().size());
    }

    @Test
    public void writeSetTest() throws Exception {
        String key = "hello";
        Set<String> set = Sets.newHashSet("hello", "world");
        JodisObject obj = new JodisObject(new JodisSet(set), KeyType.JODIS_SET, EncodingType.OBJ_ENCODING_SET.getType());
        obj.setExpireTime(0);

        ByteBuffer buf = jdbWriter.writeSet(key, obj);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        sub.flip();  // 切换到读取模式

        KeyValue<String, JodisObject> keyValue = jdbReader.readSet(sub);
        Assert.assertEquals(key, keyValue.getKey());
        Assert.assertEquals(2, ((JodisSet)keyValue.getValue().getValue()).getHolder().size());
    }

    @Test
    public void writeMapTest() throws Exception {
        String key = "hello";

        Map<String, String> map = Maps.newHashMap();
        map.put("hello", "world");
        map.put("world", "hello");

        JodisObject obj = new JodisObject(new JodisHash(map), KeyType.JODIS_HASH, EncodingType.OBJ_ENCODING_HT.getType());
        obj.setExpireTime(0);

        ByteBuffer buf = jdbWriter.writeHash(key, obj);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        sub.flip();  // 切换到读取模式

        KeyValue<String, JodisObject> keyValue = jdbReader.readMap(sub);
        Assert.assertEquals(key, keyValue.getKey());
        Assert.assertEquals(2, ((JodisHash)keyValue.getValue().getValue()).getHolder().size());
    }

    @Test
    public void writeZSetTest() throws Exception {
        String key = "hello";

        Map<String, Double> map = Maps.newHashMap();
        map.put("hello", 0.1);
        map.put("world", 0.2);

        JodisObject obj = new JodisObject(new JodisSortedSet(map), KeyType.JODIS_ZSET, EncodingType.OBJ_ENCODING_SKIPLIST.getType());
        obj.setExpireTime(0);

        ByteBuffer buf = jdbWriter.writeZSet(key, obj);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        sub.flip();  // 切换到读取模式

        KeyValue<String, JodisObject> keyValue = jdbReader.readZSet(sub);
        Assert.assertEquals(key, keyValue.getKey());
        Assert.assertEquals(2, ((JodisSortedSet)keyValue.getValue().getValue()).getHolder().size());
    }
}
