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
        Assert.assertEquals("jodis_value", stringOperation.get("jodis1"));
    }

    @Test
    public void getRangeTest() {
        Assert.assertEquals(StringUtils.EMPTY, stringOperation.getRange("hello", 0, 3));
        Assert.assertEquals(5, stringOperation.set("hello", "world"));
        Assert.assertEquals(StringUtils.EMPTY, stringOperation.getRange("hello", 2, 1));
        Assert.assertEquals("o", stringOperation.getRange("hello", 1, 1));
        Assert.assertEquals("world", stringOperation.getRange("hello", -1, 6));
        Assert.assertEquals("d", stringOperation.getRange("hello", 4, 4));
        Assert.assertEquals(1, stringOperation.getRange("hello", 0, 0).length());
    }

    @Test
    public void getAndSetTest() {
        stringOperation.set("jodis1", "jodis_value");
        Assert.assertEquals("jodis_value", stringOperation.get("jodis1"));
        Assert.assertNull(stringOperation.getAndSet("jodis2", "jodis_value2"));
        Assert.assertEquals("jodis_value2", stringOperation.get("jodis2"));
        Assert.assertEquals("jodis_value2", stringOperation.getAndSet("jodis2", "jodis_value3"));
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
        Assert.assertEquals("jodis_value".length(), stringOperation.strLen("jodis1"));
        Assert.assertEquals("jodis_value", stringOperation.get("jodis1"));
        Assert.assertEquals(0, stringOperation.strLen("jodis2"));
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
        Assert.assertEquals(1, stringOperation.increment("jodis1"));
        Assert.assertEquals(0, stringOperation.decrement("jodis1"));
        Assert.assertEquals(2, stringOperation.incrementBy("jodis1", 2));
        Assert.assertEquals(0, stringOperation.decrementBy("jodis1", 2));
    }

    @Test
    public void incrementByFloatTest() {
        Assert.assertEquals(1.0F, stringOperation.incrementByFloat("jodis1", 1.0F), 0.01F);
        Assert.assertEquals(6.21F, stringOperation.incrementByFloat("jodis1", 5.21F), 0.01F);
        Assert.assertEquals(-6.93F, stringOperation.incrementByFloat("jodis1", -13.14F), 0.01F);
    }

    @Test
    public void multiGetTest() {
        stringOperation.set("jodis1", "jodis_value1");
        stringOperation.set("jodis2", "jodis_value2");

        java.util.List<String> result = stringOperation.multiGet(Lists.newArrayList("jodis1", "jodis2"));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("jodis_value1", result.get(0));
        Assert.assertEquals("jodis_value2", result.get(1));
    }

    @Test
    public void multiSetTest() {
        stringOperation.multiSet(Lists.newArrayList("jodis1", "jodis_value1", "jodis2", "jodis_value2"));
        java.util.List<String> result = stringOperation.multiGet(Lists.newArrayList("jodis1", "jodis2"));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("jodis_value1", result.get(0));
        Assert.assertEquals("jodis_value2", result.get(1));
    }
}
