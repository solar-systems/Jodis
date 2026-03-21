package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

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
    public void setScardTest() {
        setOperation.setAdd("jodis1", "jodis_value_1");
        Assert.assertEquals(1, setOperation.setCard("jodis1"));
        setOperation.setAdd("jodis1", "jodis_value_1");
        Assert.assertEquals(1, setOperation.setCard("jodis1"));
        setOperation.setAdd("jodis1", "jodis_value_2");
        Assert.assertEquals(2, setOperation.setCard("jodis1"));
    }

    @Test
    public void setDiffTest() {
        setOperation.setAdd("jodis1", "jodis_value_1");
        setOperation.setAdd("jodis1", "jodis_value_2");
        setOperation.setAdd("jodis2", "jodis_value_1");
        setOperation.setAdd("jodis2", "jodis_value_3");
        List<String> list = setOperation.setDiff("jodis1", "jodis2");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void setUnionTest() {
        setOperation.setAdd("jodis1", "jodis_value_1");
        setOperation.setAdd("jodis1", "jodis_value_2");
        setOperation.setAdd("jodis2", "jodis_value_1");
        setOperation.setAdd("jodis2", "jodis_value_3");
        List<String> list = setOperation.setUnion("jodis1", "jodis2");
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void setInterTest() {
        setOperation.setAdd("jodis1", "jodis_value_1");
        setOperation.setAdd("jodis1", "jodis_value_2");
        setOperation.setAdd("jodis2", "jodis_value_1");
        setOperation.setAdd("jodis2", "jodis_value_3");
        List<String> list = setOperation.setInter("jodis1", "jodis2");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void setIsMemberTest() {
        Assert.assertFalse(setOperation.setIsMember("jodis1", "jodis_value_1"));
        setOperation.setAdd("jodis1", "jodis_value_1");
        Assert.assertTrue(setOperation.setIsMember("jodis1", "jodis_value_1"));
        Assert.assertFalse(setOperation.setIsMember("jodis1", "jodis_value_2"));
        setOperation.setAdd("jodis1", "jodis_value_2");
        Assert.assertTrue(setOperation.setIsMember("jodis1", "jodis_value_2"));
    }

    @Test
    public void setMoveTest() {
        setOperation.setAdd("jodis1", "jodis_value_1");
        setOperation.setAdd("jodis1", "jodis_value_2");
        setOperation.setAdd("jodis2", "jodis_value_1");
        setOperation.setAdd("jodis2", "jodis_value_3");

        Assert.assertTrue(setOperation.setIsMember("jodis1", "jodis_value_2"));
        Assert.assertTrue(setOperation.setMove("jodis1", "jodis2","jodis_value_2"));
        Assert.assertFalse(setOperation.setIsMember("jodis1", "jodis_value_2"));
        Assert.assertTrue(setOperation.setIsMember("jodis2", "jodis_value_2"));
    }

    @Test
    public void setRemoveTest() {
        setOperation.setAdd("jodis1", "jodis_value");
        Assert.assertEquals(1, setOperation.setMembers("jodis1").size());
        Assert.assertTrue(setOperation.setRemove("jodis1", "jodis_value"));
        Assert.assertEquals(0, setOperation.setMembers("jodis1").size());
    }

    @Test
    public void setPopTest() {
        setOperation.setAdd("jodis1", "jodis_value");
        setOperation.setAdd("jodis1", "jodis_value2");
        Assert.assertNotNull(setOperation.setPop("jodis1"));
        Assert.assertNotNull(setOperation.setPop("jodis1"));
        Assert.assertEquals(setOperation.setPop("jodis1"), StringUtils.NIL);
    }

    @Test
    public void setRandMemberTest() {
        Assert.assertEquals(setOperation.setRandMember("jodis1"), StringUtils.NIL);
        setOperation.setAdd("jodis1", "jodis_value");
        setOperation.setAdd("jodis1", "jodis_value2");
        Assert.assertNotNull(setOperation.setRandMember("jodis1"));
        Assert.assertNotNull(setOperation.setRandMember("jodis1"));

    }
}
