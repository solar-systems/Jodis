package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-08-08 16:29
 *
 */
public class Utils {
    private Utils() {}

    /**
     * @param s
     * @return
     */
    public static long toTimestamp(String s) {
        try {
            return Long.parseLong(s);
        }catch (NumberFormatException e) {
            return -1L;
        }
    }

    /**
     * todo 如果解析失败需要返回一个特定的值
     * @param s
     * @return
     */
    public static int toInt(String s) {
        return 0;
    }

    /**
     * todo
     * @param s
     * @return
     */
    public static float toFloat(String s) {
        return 0;
    }

    /**
     * todo
     * @param s
     * @return
     */
    public static double toDouble(String s) {
        return 0;
    }
}
