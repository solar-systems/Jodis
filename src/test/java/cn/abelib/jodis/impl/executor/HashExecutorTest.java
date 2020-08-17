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
 * @Date: 2020-08-09 16:53
 */
public class HashExecutorTest {
    private JodisDb jodisDb;
    private HashExecutor hashExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        hashExecutor = new HashExecutor(jodisDb);
    }

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.HASH_HSET, Lists.newArrayList("hello", "hello1", "world1"));
        Response response1 = hashExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.HASH_HSET, Lists.newArrayList("hello", "hello2", "world2"));
        Response response2 = hashExecutor.execute(request2);
        System.err.println(response2.toRespString());

        Request request3 = new Request(ProtocolConstant.HASH_HGETALL, Lists.newArrayList("hello"));
        Response response3 = hashExecutor.execute(request3);
        System.err.println(response3.toRespString());

        Request request4 = new Request(ProtocolConstant.HASH_HVALS, Lists.newArrayList("hello"));
        Response response4 = hashExecutor.execute(request4);
        System.err.println(response4.toRespString());
    }
}
