package cn.abelib.jodis.utils;

import org.junit.Assert;
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
        java.nio.file.Path path = IoUtils.createFileIfNotExists("log", "default.wal");
        Assert.assertNotNull(path);
        Assert.assertTrue(java.nio.file.Files.exists(path));
    }

    @Test
    public void readLinesTest() throws IOException {
        IoUtils.createFileIfNotExists("log", "default.wal");
        java.io.FileWriter writer = new java.io.FileWriter("log/default.wal");
        writer.write("test line 1\n");
        writer.write("test line 2\n");
        writer.close();
        
        Iterator<String> iterator = IoUtils.readLines("log/default.wal").iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("test line 1", iterator.next());
    }
}
