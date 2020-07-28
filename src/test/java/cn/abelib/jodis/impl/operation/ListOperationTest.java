package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
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
        Assert.assertEquals(listOperation.leftPush("jodis_list", "jodis_item1"), 1);
        Assert.assertEquals(listOperation.leftPush("jodis_list", "jodis_item2"), 2);
        Assert.assertEquals(listOperation.leftPush("jodis_list", "jodis_item3"), 3);
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
}
