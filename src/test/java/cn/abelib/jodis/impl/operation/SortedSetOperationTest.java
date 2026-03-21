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
public class SortedSetOperationTest {
    JodisDb jodisDb;
    SortedSetOperation sortedSetOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        sortedSetOperation = new SortedSetOperation(jodisDb);
    }

    @Test
    public void zaddTest() {
        sortedSetOperation.zAdd("jodis1", 1.0, "jodis_filed_1");
        sortedSetOperation.zAdd("jodis2", 2.0, "jodis_filed_2");
        sortedSetOperation.zAdd("jodis3", 3.0, "jodis_filed_3");

        Assert.assertEquals(1.0, sortedSetOperation.zScore("jodis1", "jodis_filed_1"), 0.01);
        Assert.assertEquals(2.0, sortedSetOperation.zScore("jodis2", "jodis_filed_2"), 0.01);
        Assert.assertEquals(3.0, sortedSetOperation.zScore("jodis3", "jodis_filed_3"), 0.01);
        Assert.assertEquals(3, sortedSetOperation.size());
    }

    @Test
    public void zCardTest() {
        sortedSetOperation.zAdd("jodis1", 1.0, "jodis_filed_1");
        sortedSetOperation.zAdd("jodis1", 2.0, "jodis_filed_2");
        sortedSetOperation.zAdd("jodis1", 3.0, "jodis_filed_3");

        Assert.assertEquals(3, sortedSetOperation.zCard("jodis1"));
        Assert.assertEquals(1, sortedSetOperation.size());
    }

    @Test
    public void zCountTest() {
        sortedSetOperation.zAdd("jodis1", 1.0, "jodis_filed_1");
        sortedSetOperation.zAdd("jodis1", 2.0, "jodis_filed_2");
        sortedSetOperation.zAdd("jodis1", 3.0, "jodis_filed_3");

        Assert.assertEquals(sortedSetOperation.zCount("jodis1", 1.0, 2.0), 2);
    }

    @Test
    public void zRemoveTest() {
        sortedSetOperation.zAdd("jodis1", 1.0, "jodis_filed_1");
        sortedSetOperation.zAdd("jodis1", 2.0, "jodis_filed_2");
        sortedSetOperation.zAdd("jodis1", 3.0, "jodis_filed_3");

        Assert.assertEquals(1, sortedSetOperation.zRemove("jodis1", "jodis_filed_1"));
        Assert.assertEquals(2, sortedSetOperation.zCard("jodis1"));
    }
}
