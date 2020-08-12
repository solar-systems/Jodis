package cn.abelib.jodis.utils;

import org.junit.Test;

/**
 * @Author: abel.huang
 * @Date: 2020-08-12 22:45
 */
public class StringUtilsTest {

    @Test
    public void formatTest() {
        String str = StringUtils.format("Jodis{}Jodis{}Jodis", "-", "-");
        System.err.println(str);
    }
}
