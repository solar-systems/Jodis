package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 23:16
 */
public class HashOperationTest {
    JodisDb jodisDb;
    HashOperation hashOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        hashOperation = new HashOperation(jodisDb);
    }

    @Test
    public void hashSetTest() {
        Assert.assertTrue(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        Assert.assertFalse(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        System.err.println(hashOperation.getHash("jodis_key_1"));
        Assert.assertSame(hashOperation.hashGet("jodis_key_1", "jodis_field_1"), "jodis_value_1");
        Assert.assertSame(hashOperation.hashGet("jodis_key_1", "jodis_field_2"), StringUtils.NIL);
    }

    @Test
    public void hashGetAllTest() {
        Assert.assertSame(hashOperation.hashGetAll("jodis_key_1").size(), 0);
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        Assert.assertSame(hashOperation.hashGetAll("jodis_key_1").size(), 2);
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        Assert.assertSame(hashOperation.hashGetAll("jodis_key_1").size(), 4);
    }

    @Test
    public void hashExistsTest() {
        Assert.assertFalse(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
        Assert.assertTrue(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        Assert.assertTrue(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
    }

    @Test
    public void hashDeleteTest() {
        Assert.assertSame(hashOperation.hashDelete("jodis_key_1", "jodis_field_1"), 0);
        Assert.assertTrue(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        Assert.assertTrue(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
        Assert.assertSame(hashOperation.hashDelete("jodis_key_1", "jodis_field_1"), 1);
        Assert.assertFalse(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
    }

    @Test
    public void hashKeysTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        System.err.println(hashOperation.hashKeys("jodis_key_1"));
        Assert.assertSame(hashOperation.hashKeys("jodis_key_1").size(), 2);
    }

    @Test
    public void hashValuesTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        System.err.println(hashOperation.hashValues("jodis_key_1"));
        Assert.assertSame(hashOperation.hashValues("jodis_key_1").size(), 2);
    }

    @Test
    public void hashLenTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        Assert.assertSame(hashOperation.hashLen("jodis_key_1"), 2);
    }

    @Test
    public void hashIncrementByTest() {
        hashOperation.hashIncrementBy("jodis_key_1", "jodis_field_1", 1);
        hashOperation.hashIncrementBy("jodis_key_1", "jodis_field_1", -1);
        hashOperation.hashIncrementBy("jodis_key_1", "jodis_field_1", 2);
        Assert.assertEquals(hashOperation.hashGet("jodis_key_1", "jodis_field_1"), String.valueOf(2));
    }

    @Test
    public void hashIncrementByFloatTest() {
        hashOperation.hashIncrementByFloat("jodis_key_1", "jodis_field_1", 1.1F);
        System.err.println(hashOperation.hashGet("jodis_key_1", "jodis_field_1"));
        hashOperation.hashIncrementByFloat("jodis_key_1", "jodis_field_1", -1.2F);
        System.err.println(hashOperation.hashGet("jodis_key_1", "jodis_field_1"));
        hashOperation.hashIncrementByFloat("jodis_key_1", "jodis_field_1", 2.3F);
        System.err.println(hashOperation.hashGet("jodis_key_1", "jodis_field_1"));
    }
}
