package cn.abelib.jodis.impl;

import cn.abelib.jodis.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:34
 */
public class StringOperationtTest {
    JodisDb jodisDb;
    StringOperation stringOperation;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        stringOperation = new StringOperation(jodisDb);
    }

    @Test
    public void getRangeTest() {
        Assert.assertEquals(stringOperation.getRange("hello", 0, 3), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.set("hello", "world"), 5);
        Assert.assertEquals(stringOperation.getRange("hello", 2, 1), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.getRange("hello", 1, 1), "o");
        Assert.assertEquals(stringOperation.getRange("hello", -1, 6), "world");
        Assert.assertEquals(stringOperation.getRange("hello", 4, 4), "d");
    }
}
