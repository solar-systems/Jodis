package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-10 01:25
 */
public class KeyExecutorTest {
    private JodisDb jodisDb;
    private KeyExecutor keyExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        keyExecutor = new KeyExecutor(jodisDb);
    }

    /**
     * todo
     */
    @Test
    public void executeTest() {
        Request request = new Request();
        keyExecutor.execute(request);
    }
}
