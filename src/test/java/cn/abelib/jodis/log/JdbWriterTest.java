package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.utils.KV;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        byte[] buf = jdbWriter.writeString(key, value);
        KV<String, JodisString> kv = jdbReader.readString(buf);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder(), value.getHolder());
    }

    @Test
    public void writeListTest() {
        String key = "hello";
        List<String> list = Lists.newArrayList("hello", "world");
        JodisList value = new JodisList(list);
        byte[] buf = jdbWriter.writeList(key, value);
        KV<String, JodisList> kv = jdbReader.readList(buf);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }

    @Test
    public void writeSetTest() {
        String key = "hello";
        Set<String> set = Sets.newHashSet("hello", "world");
        JodisSet value = new JodisSet(set);
        byte[] buf = jdbWriter.writeSet(key, value);
        KV<String, JodisSet> kv = jdbReader.readSet(buf);
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
        byte[] buf = jdbWriter.writeHash(key, value);
        KV<String, JodisHash> kv = jdbReader.readMap(buf);
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
        byte[] buf = jdbWriter.writeZSet(key, value);
        KV<String, JodisSortedSet> kv = jdbReader.readZSet(buf);
        Assert.assertEquals(kv.getK(), key);
        Assert.assertEquals(kv.getV().getHolder().size(), value.getHolder().size());
    }
}
