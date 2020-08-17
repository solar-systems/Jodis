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
public class SortedSetExecutorTest {
    private JodisDb jodisDb;
    private SortedSetExecutor sortedSetExecutor;

    @Before
    public void init() throws IOException {
        jodisDb = new JodisDb();
        sortedSetExecutor = new SortedSetExecutor(jodisDb);
    }

    @Test
    public void executeTest() {
        Request request1 = new Request(ProtocolConstant.ZSET_ZADD, Lists.newArrayList("hello", "0.1", "world"));
        Response response1 = sortedSetExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.ZSET_ZSCORE, Lists.newArrayList("hello", "world"));
        Response response2 = sortedSetExecutor.execute(request2);
        System.err.println(response2.toRespString());

        Request request3 = new Request(ProtocolConstant.ZSET_ZCARD, Lists.newArrayList("hello"));
        Response response3 = sortedSetExecutor.execute(request3);
        System.err.println(response3.toRespString());
    }
}
