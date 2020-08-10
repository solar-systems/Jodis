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

        System.out.println(sortedSetOperation.zScore("jodis1", "jodis_filed_1"));
        System.out.println(sortedSetOperation.zScore("jodis2", "jodis_filed_2"));
        System.out.println(sortedSetOperation.zScore("jodis3", "jodis_filed_3"));
        Assert.assertEquals(sortedSetOperation.size(), 3);
    }

    @Test
    public void zCardTest() {
        sortedSetOperation.zAdd("jodis1", 1.0, "jodis_filed_1");
        sortedSetOperation.zAdd("jodis1", 2.0, "jodis_filed_2");
        sortedSetOperation.zAdd("jodis1", 3.0, "jodis_filed_3");

        System.out.println(sortedSetOperation.zCard("jodis1"));
        Assert.assertEquals(sortedSetOperation.size(), 1);
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

        Assert.assertEquals(sortedSetOperation.zRemove("jodis1", "jodis_filed_1"), 1);
        System.out.println(sortedSetOperation.getJodisZSet("jodis1").getHolder());
        System.out.println(sortedSetOperation.getJodisZSet("jodis1").getSkipList().scores());
        System.out.println(sortedSetOperation.getJodisZSet("jodis1").getSkipList().values());
    }
}
