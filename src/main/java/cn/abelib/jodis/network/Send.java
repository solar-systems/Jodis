package cn.abelib.jodis.network;

import java.nio.channels.SocketChannel;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:46
 */
public interface Send {
    int writeTo(SocketChannel socketChannel);

    boolean complete();
}
