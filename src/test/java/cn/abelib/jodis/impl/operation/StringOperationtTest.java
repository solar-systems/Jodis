package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:34
 */
public class StringOperationtTest {
    JodisDb jodisDb;
    StringOperation stringOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        stringOperation = new StringOperation(jodisDb);
    }

    @Test
    public void setGetTest() {
        Assert.assertFalse(stringOperation.exists("jodis1"));
        stringOperation.set("jodis1", "jodis_value");
        Assert.assertTrue(stringOperation.exists("jodis1"));
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_value");
    }

    @Test
    public void getRangeTest() {
        Assert.assertEquals(stringOperation.getRange("hello", 0, 3), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.set("hello", "world"), 5);
        Assert.assertEquals(stringOperation.getRange("hello", 2, 1), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.getRange("hello", 1, 1), "o");
        Assert.assertEquals(stringOperation.getRange("hello", -1, 6), "world");
        Assert.assertEquals(stringOperation.getRange("hello", 4, 4), "d");
        Assert.assertSame(stringOperation.getRange("hello", 0, 0).length(), 1);
    }

    @Test
    public void getAndSetTest() {
        stringOperation.set("jodis1", "jodis_value");
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_value");
        Assert.assertNull( stringOperation.getAndSet("jodis2", "jodis_value2"));
        Assert.assertEquals(stringOperation.get("jodis2"), "jodis_value2");
        Assert.assertEquals(stringOperation.getAndSet("jodis2", "jodis_value3"), "jodis_value2");
    }

    @Test
    public void setIfNotExistsTest() {
        Assert.assertFalse(stringOperation.exists("jodis1"));
        Assert.assertTrue(stringOperation.setIfNotExists("jodis1", "jodis_value"));
        Assert.assertTrue(stringOperation.exists("jodis1"));
        Assert.assertFalse(stringOperation.setIfNotExists("jodis1", "jodis_value2"));
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_value");
    }

    @Test
    public void strLenTest() {
        stringOperation.set("jodis1", "jodis_value");
        Assert.assertSame(stringOperation.strLen("jodis1"), "jodis_value".length());
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_value");
        Assert.assertSame(stringOperation.strLen("jodis2"), 0);
    }

    @Test
    public void appendTest() {
        stringOperation.append("jodis1", "jodis_value");
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_value");
        stringOperation.append("jodis1", "jodis_value");
        Assert.assertEquals(stringOperation.get("jodis1"), "jodis_valuejodis_value");
    }

    @Test
    public void incrementTest() {
        Assert.assertSame(stringOperation.increment("jodis1"), 1);
        Assert.assertSame(stringOperation.decrement("jodis1"), 0);
        Assert.assertSame(stringOperation.incrementBy("jodis1", 2), 2);
        Assert.assertSame(stringOperation.decrementBy("jodis1", 2), 0);
    }

    @Test
    public void incrementByFloatTest() {
        System.out.println(stringOperation.incrementByFloat("jodis1", 1.0F));
        System.out.println(stringOperation.incrementByFloat("jodis1", 5.21F));
        System.out.println(stringOperation.incrementByFloat("jodis1", -13.14F));
    }

    @Test
    public void multiGetTest() {
        stringOperation.set("jodis1", "jodis_value1");
        stringOperation.set("jodis2", "jodis_value2");

        System.err.println(stringOperation.multiGet(Lists.newArrayList("jodis1", "jodis2")));
    }

    @Test
    public void multiSetTest() {
        stringOperation.multiSet(Lists.newArrayList("jodis1", "jodis_value1", "jodis2", "jodis_value2"));
        System.err.println(stringOperation.multiGet(Lists.newArrayList("jodis1", "jodis2")));
    }
}
