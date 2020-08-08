package cn.abelib.jodis.protocol;

import cn.abelib.jodis.network.Send;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:48
 */
public class JodisSend implements Send {
    private Logger logger = Logger.getLogger(getClass());

    private boolean completed = false;

    private Response response;

    public JodisSend(Response response) {
        this.response = response;
    }

    /**
     * Response 写回
     * @param socketChannel
     * @return
     */
    @Override
    public int write(SocketChannel socketChannel) {
        int write = 0;
        try {
            if (Objects.isNull(response)) {
                response = ErrorResponse.errorCommon();
            }
            byte[] bytes = ByteUtils.getBytesUTF8(response.toRespString());
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            write = socketChannel.write(buffer);
        } catch (IOException e) {
            logger.error(e);
        }
        completed = true;
        return write;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }
}
