package cn.abelib.jodis.network.jodis;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.network.Receive;
import cn.abelib.jodis.network.RequestHandler;
import cn.abelib.jodis.network.Send;

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
        buffer.flip();
        System.err.println("Receive total bytes:  " + (buffer.remaining() - 1));


        return new JodisSend();
    }
}
