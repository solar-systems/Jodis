package cn.abelib.jodis.impl;

import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-08-11 22:38
 */
public class JodisDbTest {
    JodisDb jodisDb;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
    }

    @Test
    public void executeTest() throws IOException {
        Request request1 = new Request(ProtocolConstant.STRING_SET, Lists.newArrayList("hello", "world"));
        Response response1 = jodisDb.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.STRING_GET, Lists.newArrayList("hello"));
        Response response2 = jodisDb.execute(request2);
        System.err.println(response2.toRespString());
    }
}
