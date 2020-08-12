package cn.abelib.jodis.utils;

import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

/**
 * @Author: abel.huang
 * @Date: 2020-07-18 01:00
 */
public class IoUtilsTest {

    @Test
    public void addTest() throws IOException {
        IoUtils.createFileIfNotExists("log", "default.wal");
    }

    @Test
    public void readLinesTest() throws IOException {
        Iterator<String> iterator = IoUtils.readLines("log/default.wal").iterator();
        while (iterator.hasNext()) {
            System.err.println(iterator.next());
        }
    }
}
