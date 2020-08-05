package cn.abelib.jodis.network.jodis;

import cn.abelib.jodis.network.Receive;
import cn.abelib.jodis.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:47
 */
public class JodisReceive implements Receive {
    private final Logger logger = Logger.getLogger(getClass());

    /**
     * 缓冲区
     */
    private ByteBuffer byteBuffer;
    /**
     *  todo 暂时不需要如此复杂的设计
     * 上次缓冲区遗留的数据
     */
    private ByteBuffer leftBuffer;

    private boolean completed;

    /**
     * maxRequestSize means
     * 服务端缓冲区大小
     * @param maxRequestSize
     */
    public JodisReceive(int maxRequestSize) {
        byteBuffer = ByteBuffer.allocate(maxRequestSize);
        completed = false;
    }

    @Override
    public int readFrom(SocketChannel socketChannel) {
        int read = 0;
        byteBuffer.clear();
        try {
             read = socketChannel.read(byteBuffer);
        } catch (IOException e) {
            logger.warn(e);
        }
        this.completed = true;
        return read;
    }

    @Override
    public boolean complete() {
        return this.completed;
    }

    @Override
    public ByteBuffer buffer() {
        return this.byteBuffer;
    }
}
