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

    public static String NIL = "nil";

    public static boolean isEmpty(String str) {
        return EMPTY.equals(str);
    }

    public static boolean isNotEmpty(String str) {
        return Objects.nonNull(str) && !EMPTY.equals(str);
    }

    /**
     * todo IgnoreCase?
     * @param str1
     * @param str2
     * @return
     */
    public static boolean equals(String str1, String str2) {
        if (Objects.isNull(str1) || Objects.isNull(str2)) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 首字母大写
     * @param str
     * @return
     */
    public static String capitalize(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            char firstChar = str.charAt(0);
            char newChar = Character.toTitleCase(firstChar);
            if (firstChar == newChar) {
                return str;
            } else {
                char[] newChars = new char[strLen];
                newChars[0] = newChar;
                str.getChars(1, strLen, newChars, 1);
                return String.valueOf(newChars);
            }
        } else {
            return str;
        }
    }
}
