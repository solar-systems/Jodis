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
 * @Date: 2020-08-13 23:16
 */
public class ServerExecutorTest {
    private JodisDb jodisDb;
    private ServerExecutor serverExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        serverExecutor = new ServerExecutor(jodisDb);
    }

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.SERVER_PING, Lists.newArrayList());
        Response response1 = serverExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.SERVER_DBSIZE, Lists.newArrayList());
        Response response2 = serverExecutor.execute(request2);
        System.err.println(response2.toRespString());

        Request request3 = new Request(ProtocolConstant.SERVER_FLUSHDB, Lists.newArrayList());
        Response response3 = serverExecutor.execute(request3);
        System.err.println(response3.toRespString());
    }
}
