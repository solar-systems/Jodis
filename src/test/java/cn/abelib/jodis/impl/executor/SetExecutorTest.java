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
public class SetExecutorTest {
    private JodisDb jodisDb;
    private SetExecutor setExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        setExecutor = new SetExecutor(jodisDb);
    }

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.SET_SADD, Lists.newArrayList("hello", "world"));
        Response response1 = setExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.SET_SADD, Lists.newArrayList("hello", "world"));
        Response response2 = setExecutor.execute(request2);
        System.err.println(response2.toRespString());

        Request request3 = new Request(ProtocolConstant.SET_SCARD, Lists.newArrayList("hello"));
        Response response3 = setExecutor.execute(request3);
        System.err.println(response3.toRespString());

        Request request4 = new Request(ProtocolConstant.SET_SADD, Lists.newArrayList("hello", "hello1"));
        Response response4 = setExecutor.execute(request4);
        System.err.println(response4.toRespString());
    }
}
