package cn.abelib.jodis.example;

import cn.abelib.jodis.EmbaddedJodis;
import cn.abelib.jodis.protocol.ProtocolConstant;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;
import com.google.common.collect.Lists;

import java.io.IOException;

/**
 * @author abel.huang
 * @date 2020/6/30 18:36
 */
public class JodisExample {
    public static void main(String[] args) throws IOException {
        EmbaddedJodis jodis = EmbaddedJodis.start("conf/jodis.properties");
        Request request = new Request(ProtocolConstant.SERVER_PING, Lists.newArrayList());
        Response response = jodis.execute(request);
        System.out.println(response);
    }
}
