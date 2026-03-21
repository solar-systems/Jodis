package cn.abelib.jodis.utils;

import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:27
 */
public class StringUtils {
    private StringUtils(){}

    public static final String STAR = "*";

    public static final String SPACE = " ";

    public static String EMPTY = "";

    public static String CLRF = "\r\n";

    public static String NIL = "nil";

    public static boolean isEmpty(String str) {
        if (Objects.isNull(str)) {
            return true;
        }
        return EMPTY.equals(str);
    }

    public static boolean isNotEmpty(String str) {
        return Objects.nonNull(str) && !EMPTY.equals(str);
    }

    /**
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

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (Objects.isNull(str1) || Objects.isNull(str2)) {
            return false;
        }
        return str1.toUpperCase().equals(str2.toUpperCase());
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

    public static String format(String pattern, Object... args) {
        return String.format(pattern.replaceAll("\\{\\}", "%s"), args);
    }

    /**
     * 简单的通配符匹配（支持 * ）
     * @param str 待匹配字符串
     * @param pattern 模式字符串（如：test*, *abc, *xyz*）
     * @return 是否匹配
     */
    public static boolean matchPattern(String str, String pattern) {
        if (isEmpty(str) || isEmpty(pattern)) {
            return false;
        }
        
        // 如果没有通配符，直接比较
        if (!pattern.contains(STAR)) {
            return str.equals(pattern);
        }
        
        // 处理通配符
        String[] parts = pattern.split("\\*");
        int index = 0;
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (isEmpty(part)) {
                continue;
            }
            
            int foundIndex = str.indexOf(part, index);
            if (foundIndex == -1) {
                return false;
            }
            
            // 如果是第一个部分且不是以*开头，必须从位置 0 开始
            if (i == 0 && !pattern.startsWith(STAR) && foundIndex != 0) {
                return false;
            }
            
            // 如果是最后一个部分且不是以*结尾，必须是字符串结尾
            if (i == parts.length - 1 && !pattern.endsWith(STAR)) {
                if (foundIndex + part.length() != str.length()) {
                    return false;
                }
            }
            
            index = foundIndex + part.length();
        }
        
        return true;
    }
}
