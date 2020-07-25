package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 23:16
 */
public class SetOperationTest {
    JodisDb jodisDb;
    SetOperation setOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        setOperation = new SetOperation(jodisDb);
    }

    @Test
    public void setAddTest() {
        setOperation.setAdd("jodis1", "jodis_value");
        Assert.assertTrue(setOperation.exists("jodis1"));
        setOperation.setAdd("jodis1", "jodis_value");
        Assert.assertEquals(setOperation.size(), 1);
    }

    @Test
    public void setRemoveTest() {
        setOperation.setAdd("jodis1", "jodis_value");
        Assert.assertSame(setOperation.setMembers("jodis1").size(), 1);
        Assert.assertTrue(setOperation.setRemove("jodis1", "jodis_value"));
        Assert.assertSame(setOperation.setMembers("jodis1").size(), 0);
    }

    @Test
    public void setPopTest() {
        setOperation.setAdd("jodis1", "jodis_value");
        setOperation.setAdd("jodis1", "jodis_value2");
        Assert.assertNotNull(setOperation.setPop("jodis1"));
        Assert.assertNotNull(setOperation.setPop("jodis1"));
        Assert.assertNull(setOperation.setPop("jodis1"));
    }

}
