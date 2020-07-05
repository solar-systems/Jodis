package cn.abelib.jodis.impl;

import cn.abelib.jodis.Jodis;
import cn.abelib.jodis.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:34
 */
public class StringOperationtTest {
    Jodis jodis;
    StringOperation stringOperation;

    @Before
    public void init() {
        jodis = new Jodis();
        stringOperation = new StringOperation(jodis);
    }

    @Test
    public void getRangeTest() {
        Assert.assertEquals(stringOperation.getRange("hello", 0, 3), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.put("hello", "world"), 5);
        Assert.assertEquals(stringOperation.getRange("hello", 2, 1), StringUtils.EMPTY);
        Assert.assertEquals(stringOperation.getRange("hello", 1, 1), "o");
        Assert.assertEquals(stringOperation.getRange("hello", -1, 6), "world");
        Assert.assertEquals(stringOperation.getRange("hello", 4, 4), "d");
    }
}
