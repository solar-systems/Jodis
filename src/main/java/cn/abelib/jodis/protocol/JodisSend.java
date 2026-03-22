package cn.abelib.jodis.protocol;

import cn.abelib.jodis.remoting.Send;
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
            byte[] respBytes = ByteUtils.getBytesUTF8(response.toRespString());

            // 创建包含4字节长度前缀的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(4 + respBytes.length);
            buffer.putInt(respBytes.length);  // 写入4字节长度前缀
            buffer.put(respBytes);             // 写入响应数据
            buffer.flip();

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
