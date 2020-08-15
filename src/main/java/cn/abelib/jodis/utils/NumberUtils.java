package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-08-08 16:29
 *
 */
public class NumberUtils {
    private NumberUtils() {}

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
     * 如果解析失败需要返回一个特定的值
     * @param num
     * @return
     */
    public static Integer toInt(String num) {
        Integer ans;
        try {
            ans = Integer.parseInt(num);
        }catch (NumberFormatException e) {
            // ignore
            ans = null;
        }
        return ans;
    }

    /**
     * @param num
     * @return
     */
    public static Float toFloat(String num) {
        Float ans;
        try {
            ans = Float.parseFloat(num);
        }catch (NumberFormatException e) {
            // ignore
            ans = null;
        }
        return ans;
    }

    /**
     * @param num
     * @return
     */
    public static Double toDouble(String num) {
        Double ans;
        try {
            ans = Double.parseDouble(num);
        }catch (NumberFormatException e) {
            // ignore
            ans = null;
        }
        return ans;
    }
}
