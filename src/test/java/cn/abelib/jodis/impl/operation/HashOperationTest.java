package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

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
        Assert.assertEquals(hashOperation.hashGet("jodis_key_1", "jodis_field_1"), "jodis_value_1");
        Assert.assertEquals(StringUtils.NIL, hashOperation.hashGet("jodis_key_1", "jodis_field_2"));
    }

    @Test
    public void hashGetAllTest() {
        Assert.assertEquals(0, hashOperation.hashGetAll("jodis_key_1").size());
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        Assert.assertEquals(2, hashOperation.hashGetAll("jodis_key_1").size());
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        Assert.assertEquals(4, hashOperation.hashGetAll("jodis_key_1").size());
    }

    @Test
    public void hashExistsTest() {
        Assert.assertFalse(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
        Assert.assertTrue(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        Assert.assertTrue(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
    }

    @Test
    public void hashDeleteTest() {
        Assert.assertEquals(0, hashOperation.hashDelete("jodis_key_1", "jodis_field_1"));
        Assert.assertTrue(hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1"));
        Assert.assertTrue(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
        Assert.assertEquals(1, hashOperation.hashDelete("jodis_key_1", "jodis_field_1"));
        Assert.assertFalse(hashOperation.hashExists("jodis_key_1", "jodis_field_1"));
    }

    @Test
    public void hashKeysTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        System.err.println(hashOperation.hashKeys("jodis_key_1"));
        Assert.assertEquals(2, hashOperation.hashKeys("jodis_key_1").size());
    }

    @Test
    public void hashValuesTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        System.err.println(hashOperation.hashValues("jodis_key_1"));
        Assert.assertEquals(2, hashOperation.hashValues("jodis_key_1").size());
    }

    @Test
    public void hashLenTest() {
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_1", "jodis_value_1");
        hashOperation.hashSet("jodis_key_1", "jodis_field_2", "jodis_value_2");
        Assert.assertEquals(2, hashOperation.hashLen("jodis_key_1"));
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

    @Test
    public void hashMultiSetTest() {
        hashOperation.hashMultiSet("jodis_key_1", Lists.newArrayList("jodis_field_1", "jodis_value_1",
                "jodis_field_2", "jodis_value_2", "jodis_field_3", "jodis_value_3"));
        System.err.println(hashOperation.getHash("jodis_key_1"));
    }

    @Test
    public void hashMultiGetTest() {
        hashOperation.hashMultiSet("jodis_key_1", Lists.newArrayList("jodis_field_1", "jodis_value_1",
                "jodis_field_2", "jodis_value_2", "jodis_field_3", "jodis_value_3"));
        List<String> list = hashOperation.hashMultiGet("jodis_key_1", Lists.newArrayList("jodis_field_1", "jodis_field_2", "jodis_field_3"));
        System.err.println(list);
    }
}
