package cn.abelib.jodis.network;


import cn.abelib.jodis.utils.Closeables;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:41
 */
public class Accepter extends AbstractServerThread {
    private int port;
    private Processor processor;

    public Accepter(int port, Processor processor) {
        this.port = port;
        this.processor = processor;
    }

    @Override
    public void run() {
        ServerSocketChannel serverSocketChannel = null;

        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 设置非阻塞
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(getSelector(), SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            logger.error(e);
        }

        logger.info("waiting connection on port: {}", port);

        startupComplete();

        while(isRunning()) {
            int ready;
            try {
                ready = getSelector().select(500L);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            if(ready <= 0) {
                continue;
            }
            Iterator<SelectionKey> iter = getSelector().selectedKeys().iterator();
            while(iter.hasNext() && isRunning()) {
                try {
                    SelectionKey key = iter.next();
                    iter.remove();
                    //
                    if(key.isAcceptable()) {
                        accept(key, processor);
                    }else {
                        throw new IllegalStateException("Unrecognized key state for acceptor thread.");
                    }
                } catch (Throwable t) {
                    logger.error("Error in acceptor {}", Throwables.getStackTraceAsString(t));
                }
            }
        }
        //run over
        logger.info("Closing server socket and selector.");
        Closeables.closeQuietly(serverSocketChannel);
        Closeables.closeQuietly(getSelector());
        shutdownComplete();
    }

    private void accept(SelectionKey key, Processor processor) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        serverSocketChannel.socket().setReceiveBufferSize(1024 * 1024);

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setSendBufferSize(1024 * 1024);

        processor.accept(socketChannel);
    }
}
