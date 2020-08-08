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
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
        return charBuffer.toString();
    }
}
