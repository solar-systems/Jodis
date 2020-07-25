package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.operation.ListOperation;
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
        Assert.assertEquals(listOperation.leftPush("hello", "world"), 1);
        Assert.assertEquals(listOperation.leftPush("hello", "Jodis"), 2);
        Assert.assertEquals(listOperation.leftPush("hello", "Java"), 3);
    }
}
