package cn.abelib.jodis.network;

import java.nio.channels.SocketChannel;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:46
 */
public interface Send {
    /**
     * write to SocketChannel
     * @param socketChannel
     * @return
     */
    int write(SocketChannel socketChannel);

    /**
     * completed write data
     * @return
     */
    boolean completed();
}
