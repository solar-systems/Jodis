package cn.abelib.jodis.log;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

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
    }

    @Test
    public void readWalTest() throws IOException {
        Iterator<String> iterator = walReader.readWal();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
