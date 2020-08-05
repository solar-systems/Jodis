package cn.abelib.jodis.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @Author: abel.huang
 * @Date: 2020-08-02 22:53
 */
public class Closeables {
    final static Logger logger = Logger.getLogger(Closeables.class);

    private Closeables() {}

    public static void closeQuietly(InputStream inputStream) {
        com.google.common.io.Closeables.closeQuietly(inputStream);
    }

    public static void closeQuietly(Reader reader) {
        com.google.common.io.Closeables.closeQuietly(reader);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            com.google.common.io.Closeables.close(closeable, true);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
