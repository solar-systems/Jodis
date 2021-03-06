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
 * @Date: 2020-08-09 16:52
 */
public class ListExecutorTest {
    private JodisDb jodisDb;
    private ListExecutor listExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        listExecutor = new ListExecutor(jodisDb);
    }

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.LIST_LPUSH, Lists.newArrayList("hello", "world"));
        Response response1 = listExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.LIST_LPUSH, Lists.newArrayList("hello", "Jodis"));
        Response response2 = listExecutor.execute(request2);
        System.err.println(response2.toRespString());

        Request request3 = new Request(ProtocolConstant.LIST_LRANGE, Lists.newArrayList("hello", "0", "1"));
        Response response3 = listExecutor.execute(request3);
        System.err.println(response3.toRespString());
    }
}
