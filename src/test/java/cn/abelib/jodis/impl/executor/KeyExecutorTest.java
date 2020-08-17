package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;
import com.google.common.collect.Lists;
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

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.KEY_EXISTS, Lists.newArrayList("hello"));
        Response response1 = keyExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.KEY_KEYS, Lists.newArrayList("hello"));
        Response response2 = keyExecutor.execute(request2);
        System.err.println(response2.toRespString());
    }
}
