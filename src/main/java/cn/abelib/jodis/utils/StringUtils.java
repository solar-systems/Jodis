package cn.abelib.jodis.utils;

import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:27
 */
public class StringUtils {
    private StringUtils(){}

    public static String EMPTY = "";

    public static String CLRF = "\r\n";

    public static boolean isEmpty(String str) {
        return EMPTY.equals(str);
    }

    public static boolean isNotEmpty(String str) {
        return Objects.nonNull(str) && !EMPTY.equals(str);
    }

    public static boolean equals(String str1, String str2) {
        if (Objects.isNull(str1) || Objects.isNull(str2)) {
            return false;
        }
        return str1.equals(str2);
    }
}
