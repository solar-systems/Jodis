package cn.abelib.jodis.impl;

import cn.abelib.jodis.utils.KeyValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author: abel.huang
 * @Date: 2020-07-12 01:39
 */
public class SkipListTest {
    SkipList skipList;

    @Before
    public void init() {
        skipList = new SkipList();
    }

    @Test
    public void addTest() {
        skipList.add(1, "Hello");
        skipList.add(3, "World");
        skipList.add(2, "Skip");
        skipList.add(4, "List");
        Assert.assertSame(skipList.size(),4);

        System.err.println(skipList.values());
    }

    @Test
    public void findTest() {
        skipList.find(1);
        skipList.add(1, "Hello");
        skipList.add(3, "World");
        skipList.add(2, "Skip");
        skipList.add(4, "List");

        Assert.assertEquals(skipList.find(1), "Hello");
        Assert.assertEquals(skipList.find(2), "Skip");
        Assert.assertEquals(skipList.find(3), "World");
        Assert.assertEquals(skipList.find(4), "List");
    }

    @Test
    public void deleteTest() {
        skipList.add(1, "Hello");
        skipList.add(3, "World");
        skipList.add(2, "Skip");
        skipList.add(4, "List");

        skipList.delete(4);
        Assert.assertSame(skipList.size(),3);
        Assert.assertNull(skipList.find(4));

        skipList.delete(3);
        Assert.assertSame(skipList.size(),2);
        Assert.assertNull(skipList.find(3));
    }

    @Test
    public void iteratorTest() {
        skipList.add(1, "Hello");
        skipList.add(3, "World");
        skipList.add(2, "Skip");
        skipList.add(4, "List");

        for (KeyValue<Double, String> keyValue : skipList) {
            System.err.println(keyValue);
        }
    }
}
