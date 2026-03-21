package cn.abelib.jodis.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author: abel.huang
 * @Date: 2020-07-25 22:51
 */
public class ByteUtilsTest {

    @Test
    public void test() {
        String result = ByteUtils.string2Binary("ab");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }
}
