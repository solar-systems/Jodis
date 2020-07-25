package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.KeyType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-25 16:53
 */
public class KeyOperationTest {
    JodisDb jodisDb;
    StringOperation stringOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        stringOperation = new StringOperation(jodisDb);
    }

    @Test
    public void typeTest() {
        Assert.assertNull(stringOperation.type("NOT_EXISTS_KEY"));
        stringOperation.set("string_test_key", "string_test_value");
        Assert.assertEquals(stringOperation.type("string_test_key"), KeyType.JODIS_STRING);
    }

    @Test
    public void deleteTest() {
        Assert.assertFalse(stringOperation.exists("string_test_key"));
        stringOperation.set("string_test_key", "string_test_value");
        Assert.assertTrue(stringOperation.exists("string_test_key"));
        stringOperation.delete("string_test_key");
        Assert.assertFalse(stringOperation.exists("string_test_key"));
    }

    @Test
    public void keysTest() {
        stringOperation.set("jodis1", "jodis_value1");
        stringOperation.set("jodis2", "jodis_value2");
        stringOperation.set("3jodis", "jodis_value3");
        System.out.println(stringOperation.keys("*"));
        System.out.println(stringOperation.keys("jodis"));
        Assert.assertSame(stringOperation.keys("*").size(), 3);
        Assert.assertSame(stringOperation.keys("jodis").size(), 2);
    }

    @Test
    public void renameTest() {
        Assert.assertFalse(stringOperation.rename("jodis1", "jodis2"));
        Assert.assertFalse(stringOperation.rename("jodis2", "jodis2"));
        stringOperation.set("jodis1", "jodis_value1");
        Assert.assertTrue(stringOperation.rename("jodis1", "jodis2"));
        Assert.assertFalse(stringOperation.rename("jodis2", "jodis2"));
    }

    @Test
    public void renameIfNotExistTest() {
        Assert.assertFalse(stringOperation.renameIfNotExist("jodis1", "jodis2"));
        Assert.assertFalse(stringOperation.renameIfNotExist("jodis2", "jodis2"));
        stringOperation.set("jodis1", "jodis_value1");
        Assert.assertTrue(stringOperation.renameIfNotExist("jodis1", "jodis2"));
        Assert.assertFalse(stringOperation.renameIfNotExist("jodis2", "jodis2"));
    }

    @Test
    public void randomKeyTest() {
        Assert.assertNull(stringOperation.randomKey());
        stringOperation.set("jodis1", "jodis_value1");
        stringOperation.set("jodis2", "jodis_value2");
        stringOperation.set("3jodis", "jodis_value3");
        System.out.println(stringOperation.randomKey());
        stringOperation.delete("jodis1");
        System.out.println(stringOperation.randomKey());
        Assert.assertNotNull(stringOperation.randomKey());
    }
}
