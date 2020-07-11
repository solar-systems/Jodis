package cn.abelib.jodis.impl;

import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author abel.huang
 * @date 2020/6/30 17:43
 */
public class ListOperationTest {
    JodisDb jodisDb;
    ListOperation listOperation;

    @Before
    public void init() {
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
