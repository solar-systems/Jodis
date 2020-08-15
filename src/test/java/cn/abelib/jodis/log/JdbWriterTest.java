package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.KV;
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
 * @Author: abel.huang
 * @Date: 2020-07-22 01:48
 */
public class JdbWriterTest {
    private JdbWriter jdbWriter;
    private JdbReader jdbReader;

    @Before
    public void init() {
        jdbWriter = new JdbWriter();
        jdbReader = new JdbReader();
    }

    @Test
    public void writeStringTest() {
        String key = "hello";
        JodisString value = new JodisString("world");
        ByteBuffer buffer = jdbWriter.writeString(key, value);
        ByteBuffer sub = ByteBuffer.allocate(buffer.capacity() - 5);
        buffer.position(5);
        sub.put(buffer);
        KV<String, JodisString> kv = jdbReader.readString(sub);
        Assert.assertEquals(key, kv.getK());
        Assert.assertEquals(value.getHolder(), kv.getV().getHolder());
    }

    @Test
    public void writeListTest() {
        String key = "hello";
        List<String> list = Lists.newArrayList("hello", "world");
        JodisList value = new JodisList(list);
        ByteBuffer buf = jdbWriter.writeList(key, value);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        KV<String, JodisList> kv = jdbReader.readList(sub);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }

    @Test
    public void writeSetTest() {
        String key = "hello";
        Set<String> set = Sets.newHashSet("hello", "world");
        JodisSet value = new JodisSet(set);
        ByteBuffer buf = jdbWriter.writeSet(key, value);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        KV<String, JodisSet> kv = jdbReader.readSet(sub);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }

    @Test
    public void writeMapTest() {
        String key = "hello";

        Map<String, String> map = Maps.newHashMap();
        map.put("hello", "world");
        map.put("world", "hello");

        JodisHash value = new JodisHash(map);
        ByteBuffer buf = jdbWriter.writeHash(key, value);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        KV<String, JodisHash> kv = jdbReader.readMap(sub);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }

    @Test
    public void writeZSetTest() {
        String key = "hello";

        Map<String, Double> map = Maps.newHashMap();
        map.put("hello", 0.1);
        map.put("world", 0.2);

        JodisSortedSet value = new JodisSortedSet(map);
        ByteBuffer buf = jdbWriter.writeZSet(key, value);
        ByteBuffer sub = ByteBuffer.allocate(buf.capacity() - 5);
        buf.position(5);
        sub.put(buf);
        KV<String, JodisSortedSet> kv = jdbReader.readZSet(sub);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }
}
