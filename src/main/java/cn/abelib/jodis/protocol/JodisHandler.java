package cn.abelib.jodis.protocol;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.network.Receive;
import cn.abelib.jodis.network.RequestHandler;
import cn.abelib.jodis.network.Send;
import cn.abelib.jodis.utils.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:43
 */
public class JodisHandler implements RequestHandler {
    public JodisDb jodisDb;

    public JodisHandler(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }
    @Override
    public Send handle(Receive receive) {
        ByteBuffer buffer = receive.buffer();
        String request = BufferUtils.toUTF8String(buffer);

        Response response;
        try {
            response = jodisDb.execute(request);
        } catch (IOException e) {
            response = ErrorResponse.errorCommon();
        }

        return new JodisSend(response);
    }
}
