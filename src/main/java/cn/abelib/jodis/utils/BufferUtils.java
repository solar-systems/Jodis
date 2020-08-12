package cn.abelib.jodis.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @Author: abel.huang
 * @Date: 2020-08-06 23:30
 */
public class BufferUtils {
    private BufferUtils() {}

    /**
     * ByteBuffer -> String(utf8)
     * @param buffer
     * @return
     */
    public static String toUTF8String(ByteBuffer buffer) {
        buffer.flip();
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
        String result = charBuffer.toString();
        int len = result.length();
        if (len >= 1 && result.endsWith(result)) {
            result = result.substring(0, len - 1);
        }
        return result;
    }
}
