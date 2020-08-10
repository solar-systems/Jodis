package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author abel.huang
 * @date 2020/6/30 17:43
 */
public class ListOperationTest {
    JodisDb jodisDb;
    ListOperation listOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        listOperation = new ListOperation(jodisDb);
    }

    @Test
    public void listLengthTest() {
        Assert.assertSame(listOperation.listLength("hello"), 0);
    }

    @Test
    public void leftPushTest() {
        Assert.assertEquals(listOperation. leftPush("jodis_list", "jodis_item1"), 1);
        Assert.assertEquals(listOperation.leftPush("jodis_list", "jodis_item2"), 2);
        Assert.assertEquals(listOperation.leftPush("jodis_list", "jodis_item3"), 3);
        System.err.println(listOperation.getJodisList("jodis_list").getHolder());
    }

    @Test
    public void rightPushTest() {
        Assert.assertEquals(listOperation.rightPush("jodis_list", "jodis_item1"), 1);
        Assert.assertEquals(listOperation.rightPush("jodis_list", "jodis_item2"), 2);
        Assert.assertEquals(listOperation.rightPush("jodis_list", "jodis_item3"), 3);
        System.err.println(listOperation.getJodisList("jodis_list").getHolder());
    }

    @Test
    public void leftIndexTest() {
        listOperation.leftPush("jodis_list", "jodis_item1");
        listOperation.leftPush("jodis_list", "jodis_item2");
        listOperation.leftPush("jodis_list", "jodis_item3");

        Assert.assertEquals(listOperation.leftIndex("jodis_list", 0), "jodis_item3");
        Assert.assertEquals(listOperation.leftIndex("jodis_list", 1), "jodis_item2");
        Assert.assertEquals(listOperation.leftIndex("jodis_list", 2), "jodis_item1");
    }

    @Test
    public void leftInsertTest() {
        listOperation.leftPush("jodis_list", "jodis_item1");
        listOperation.leftPush("jodis_list", "jodis_item2");
        listOperation.leftPush("jodis_list", "jodis_item3");

        Assert.assertSame(listOperation.leftInsert("jodis_list", "jodis_item1", "jodis_item1_before"), 4);
        System.err.println(listOperation.getJodisList("jodis_list").getHolder());
        Assert.assertSame(listOperation.rightInsert("jodis_list", "jodis_item1", "jodis_item1_after"), 5);
        System.err.println(listOperation.getJodisList("jodis_list").getHolder());
    }

    @Test
    public void leftPopTest() {
        listOperation.leftPush("jodis_list", "jodis_item1");
        listOperation.leftPush("jodis_list", "jodis_item2");

        Assert.assertEquals(listOperation.leftPop("jodis_list"), "jodis_item2");
        Assert.assertEquals(listOperation.leftPop("jodis_list"), "jodis_item1");
        Assert.assertEquals(listOperation.leftPop("jodis_list"), StringUtils.NIL);
    }

    @Test
    public void rightPopTest() {
        listOperation.leftPush("jodis_list", "jodis_item1");
        listOperation.leftPush("jodis_list", "jodis_item2");

        Assert.assertEquals(listOperation.rightPop("jodis_list"), "jodis_item1");
        Assert.assertEquals(listOperation.rightPop("jodis_list"), "jodis_item2");
        Assert.assertEquals(listOperation.rightPop("jodis_list"), StringUtils.NIL);
    }

    @Test
    public void listRangeTest() {
        listOperation.leftPush("jodis_list", "jodis_item1");
        listOperation.leftPush("jodis_list", "jodis_item2");
        listOperation.leftPush("jodis_list", "jodis_item3");

        System.err.println(listOperation.listRange("jodis_list", 0, 1));
        System.err.println(listOperation.listRange("jodis_list", 1, 2));
    }
}
