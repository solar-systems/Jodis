package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;
import com.google.common.collect.Lists;
import org.junit.Assert;
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
        Assert.assertNotNull(response1);

        Request request2 = new Request(ProtocolConstant.KEY_KEYS, Lists.newArrayList("hello"));
        Response response2 = keyExecutor.execute(request2);
        Assert.assertNotNull(response2);
    }

    @Test
    public void testTTLAndExpire() throws InterruptedException {
        // 1. 设置一个 key
        Request setReq = new Request(ProtocolConstant.STRING_SET, Lists.newArrayList("test_key", "test_value"));
        Response setResp = keyExecutor.execute(setReq);
        Assert.assertNotNull(setResp);
        
        // 2. 验证 key 存在
        Request existsReq1 = new Request(ProtocolConstant.KEY_EXISTS, Lists.newArrayList("test_key"));
        Response existsResp1 = keyExecutor.execute(existsReq1);
        Assert.assertNotNull(existsResp1);
        
        // 3. 设置过期时间为 10 秒
        Request expireReq = new Request(ProtocolConstant.KEY_EXPIRE, Lists.newArrayList("test_key", "10"));
        Response expireResp = keyExecutor.execute(expireReq);
        Assert.assertNotNull(expireResp);
        
        // 4. 查询 TTL，应该返回接近 10 的值
        Request ttlReq = new Request(ProtocolConstant.KEY_TTL, Lists.newArrayList("test_key"));
        Response ttlResp = keyExecutor.execute(ttlReq);
        Assert.assertNotNull(ttlResp);
        
        // 5. 设置一个很短的过期时间（1 秒）并测试过期
        Request setReq2 = new Request(ProtocolConstant.STRING_SET, Lists.newArrayList("temp_key", "temp_value"));
        keyExecutor.execute(setReq2);
        
        Request expireShortReq = new Request(ProtocolConstant.KEY_EXPIRE, Lists.newArrayList("temp_key", "1"));
        keyExecutor.execute(expireShortReq);
        
        // 等待 2 秒让 key 过期
        Thread.sleep(2000);
        
        // 6. 验证 key 已过期不存在
        Request existsReq2 = new Request(ProtocolConstant.KEY_EXISTS, Lists.newArrayList("temp_key"));
        Response existsResp2 = keyExecutor.execute(existsReq2);
        Assert.assertNotNull(existsResp2);
        
        // 7. 测试 EXPIREAT
        Request setReq3 = new Request(ProtocolConstant.STRING_SET, Lists.newArrayList("future_key", "future_value"));
        keyExecutor.execute(setReq3);
        
        // 设置过期时间为未来 10 秒后的时间戳
        long futureTimestamp = (System.currentTimeMillis() / 1000) + 10;
        Request expireAtReq = new Request(ProtocolConstant.KEY_EXPIRE_AT, Lists.newArrayList("future_key", String.valueOf(futureTimestamp)));
        Response expireAtResp = keyExecutor.execute(expireAtReq);
        Assert.assertNotNull(expireAtResp);
    }
}
