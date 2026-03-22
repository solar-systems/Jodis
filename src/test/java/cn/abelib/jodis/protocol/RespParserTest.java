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
    
    @Test
    public void parseSetexTest() {
        // SETEX key seconds value = 4 个参数（命令 + 3 个参数）
        String request = "*4\r\n" +
                "$5\r\n" +
                "SETEX\r\n" +
                "$4\r\n" +
                "temp\r\n" +
                "$2\r\n" +
                "60\r\n" +
                "$5\r\n" +
                "value\r\n";
        Request req = respParser.parse(request);
        Assert.assertNotNull(req);
        Assert.assertEquals("SETEX", req.getCommand());
        Assert.assertEquals(3, req.getArgs().size());
        Assert.assertEquals("temp", req.getArgs().get(0));
        Assert.assertEquals("60", req.getArgs().get(1));
        Assert.assertEquals("value", req.getArgs().get(2));

        // 验证生成的是内联格式（用于 WAL 存储）
        String generated = req.getRequest();
        Assert.assertNotNull(generated);
        Assert.assertEquals("SETEX temp 60 value", generated);
    }
}
