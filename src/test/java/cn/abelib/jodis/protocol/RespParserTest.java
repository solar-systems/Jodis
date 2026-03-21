package cn.abelib.jodis.protocol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author: abel.huang
 * @Date: 2020-08-15 19:13
 */
public class RespParserTest {
    private RespParser respParser;

    @Before
    public void init() {
        respParser = new RespParser();
    }

    @Test
    public void parseTest() {
        String request = "*3\r\n" +
                "$3\r\n" +
                "set\r\n" +
                "$5\r\n" +
                "hello\r\n" +
                "$6\r\n" +
                "world1";
        Request req = respParser.parse(request);
        Assert.assertNotNull(req);
        Assert.assertEquals("SET", req.getCommand());
        Assert.assertEquals(2, req.getArgs().size());
    }
}
