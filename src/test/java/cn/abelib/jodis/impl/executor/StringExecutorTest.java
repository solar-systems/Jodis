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
        Request request1 = new Request(ProtocolConstant.STRING_SET, Lists.newArrayList("hello", "world"));
        Response response1 = stringExecutor.execute(request1);
        System.err.println(response1.toRespString());

        Request request2 = new Request(ProtocolConstant.STRING_GET, Lists.newArrayList("hello"));
        Response response2 = stringExecutor.execute(request2);
        System.err.println(response2.toRespString());
    }
}
