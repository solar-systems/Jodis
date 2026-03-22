package cn.abelib.jodis.store;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-16 16:21
 */
public class WalReaderTest {
    private WalReader walReader;
    private WalWriter walWriter;

    @Before
    public void init() throws IOException {
        walReader = new WalReader("log/", "test.default.wal");
        walWriter = new WalWriter("log/", "test.default.wal");
    }

    @Test
    public void writeWalTest() throws IOException {
        walWriter.write("set hello world");
        walWriter.write("set hello world1");
        // WalWriter 成功写入，不抛出异常即表示成功
    }

    @Test
    public void readWalTest() throws IOException {
        walWriter.write("set hello world");
        walWriter.write("set hello world1");
        
        java.util.Iterator<String> iterator = walReader.readWal();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("set hello world", iterator.next());
    }
}
