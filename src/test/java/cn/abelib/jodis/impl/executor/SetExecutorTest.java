package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-09 16:52
 */
public class SetExecutorTest {
    private JodisDb jodisDb;
    private SetExecutor setExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        setExecutor = new SetExecutor(jodisDb);
    }

    /**
     * todo
     */
    @Test
    public void executeTest() {
        Request request = new Request();
        setExecutor.execute(request);
    }
}
