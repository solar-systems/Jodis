package cn.abelib.jodis.utils;

import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 23:38
 */
public class PropertiesUtils {
    private PropertiesUtils(){}

    /**
     * load properties file
     * @param filename
     * @return
     */
    public static Properties loadProps(String filename) {
        Properties props = new Properties();
        InputStream fis = null;
        Path path;
        try {
            path = Paths.get(filename);
            fis = Files.newInputStream(path);
            props.load(fis);
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(fis);
        }
    }

    /**
     * get value from properties by key
     * @param props
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(Properties props, String key, String defaultValue) {
        return props.containsKey(key) ? props.getProperty(key) : defaultValue;
    }

    public static int getInteger(Properties props, String key, int defaultValue) {
        return props.containsKey(key) ? Integer.parseInt(props.getProperty(key)) : defaultValue;
    }
}
