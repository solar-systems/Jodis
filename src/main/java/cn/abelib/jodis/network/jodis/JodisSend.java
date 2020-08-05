package cn.abelib.jodis.network.jodis;

import cn.abelib.jodis.network.Send;
import cn.abelib.jodis.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:48
 */
public class JodisSend implements Send {
    private Logger logger = Logger.getLogger(getClass());

    private boolean completed = false;

    // todo
    @Override
    public int writeTo(SocketChannel socketChannel) {
        int read = 0;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(24);
            buffer.put("Hello, World\r\n".getBytes());
            buffer.flip();
            read = socketChannel.write(buffer);
        } catch (IOException e) {
            logger.error(e);
        }
        completed = true;
        return read;
    }

    @Override
    public boolean complete() {
        return this.completed;
    }
}
