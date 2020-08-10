package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-09 16:53
 */
public class SortedSetExecutorTest {
    private JodisDb jodisDb;
    private SortedSetExecutor sortedSetExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        sortedSetExecutor = new SortedSetExecutor(jodisDb);
    }

    /**
     * todo
     */
    @Test
    public void executeTest() {
        Request request = new Request();
        sortedSetExecutor.execute(request);
    }
}
