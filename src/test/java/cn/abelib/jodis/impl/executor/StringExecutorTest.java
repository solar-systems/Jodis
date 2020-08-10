package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-09 16:51
 */
public class StringExecutorTest {
    private JodisDb jodisDb;
    private StringExecutor stringExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        stringExecutor = new StringExecutor(jodisDb);
    }

    /**
     * todo
     */
    @Test
    public void executeTest() {
        Request request = new Request();
        stringExecutor.execute(request);
    }
}
