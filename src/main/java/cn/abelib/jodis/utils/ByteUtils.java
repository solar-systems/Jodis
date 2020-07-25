package cn.abelib.jodis.utils;

import java.nio.charset.StandardCharsets;

/**
 * @Author: abel.huang
 * @Date: 2020-07-20 23:50
 */
public class ByteUtils {
    private ByteUtils() {}

    /**
     * int -> byte[]
     * @param a
     * @return
     */
    public static byte[] int2Bytes(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    /**
     * byte[] -> int
     * @param a
     * @return
     */
    public static int bytes2Int(byte[] a) {
        return (a[3] & 0xFF) |
                (a[2] & 0xFF) << 8 |
                (a[1] & 0xFF) << 16 |
                (a[0] & 0xFF) << 24;
    }

    public static String bytes2UTF8(byte[] a) {
        return new String(a, StandardCharsets.UTF_8);
    }

    public static byte[] getBytesUTF8(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String string2Binary(String str){
        char[] chars = str.toCharArray();
        StringBuilder ans = new StringBuilder();
        for(char c : chars){
            ans.append(Integer.toBinaryString(c));
        }
        return ans.toString();
    }

//    public static String binaryToString(String binary){
//        String[] tempStr = binary.split(" ");
//        char[] chars = new char[tempStr.length];
//        for(int i = 0; i < tempStr.length; i++) {
//            chars[i] = BinstrToChar(tempStr[i]);
//        }
//        return String.valueOf(chars);
//    }

    public static byte[] stringBytesWithLen(String str) {
        int len = str.length();
        byte[] lens = int2Bytes(len);
        byte[] strs = str.getBytes(StandardCharsets.UTF_8);
        return concatBytes(lens, strs);
    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
        int lenA = a.length;
        int lenB = b.length;
        byte[] concats = new byte[lenA + lenB];
        System.arraycopy(a, 0, concats, 0, lenA);
        System.arraycopy(b, 0, concats, lenA, lenB);
        return concats;
    }

    public static byte[] slice(byte[] buf, int offset, int len) {
        byte[] result = new byte[len];
        System.arraycopy(buf, offset, result, 0, len);
        return result;
    }
}
