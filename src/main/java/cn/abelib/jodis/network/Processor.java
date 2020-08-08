package cn.abelib.jodis.network;

import cn.abelib.jodis.protocol.JodisReceive;
import cn.abelib.jodis.utils.Closeables;
import cn.abelib.jodis.utils.Logger;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:40
 */
public class Processor extends AbstractServerThread {
    private final Logger logger = Logger.getLogger(getClass());

    private final BlockingQueue<SocketChannel> newConnections;
    private RequestHandler requestHandler;
    private int maxRequestSize;

    /**
     * todo maxRequestSize setting in Config File
     * @param requestHandler
     */
    public Processor(RequestHandler requestHandler) {
        this.newConnections = new ArrayBlockingQueue<>(64);
        this.requestHandler = requestHandler;
        this.maxRequestSize = 1024;
    }

    public void accept(SocketChannel socketChannel) {
        newConnections.add(socketChannel);
        getSelector().wakeup();
    }

    @Override
    public void run() {
        startupComplete();
        while (isRunning()) {
            try {
                configureNewConnections();

                final Selector selector = getSelector();
                int ready = selector.select(500);

                if (ready <= 0) {
                    continue;
                }

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext() && isRunning()) {
                    SelectionKey key = null;
                    try {
                        key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        } else if (!key.isValid()) {
                            close(key);
                        } else {
                            throw new IllegalStateException("Unrecognized key state for processor thread.");
                        }
                    } catch (InvalidRequestException e) {
                        Socket socket = channelFor(key).socket();
                        logger.info("Closing socket connection to {}:{} due to invalid request: {}",
                                socket.getInetAddress(),
                                socket.getPort(),
                                e.getMessage());
                        close(key);
                    } catch (Throwable t) {
                        Socket socket = channelFor(key).socket();
                        final String msg = "Closing socket for {}:{} because of error {}";
                        logger.info(msg, socket.getInetAddress(), socket.getPort(), Throwables.getStackTraceAsString(t));
                        close(key);
                    }
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info("Closing selector while shutting down");
        closeSelector();
        shutdownComplete();
    }

    private SocketChannel channelFor(SelectionKey key) {
        return (SocketChannel) key.channel();
    }

    private void close(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        logger.info("Closing connection from {}", channel.socket().getRemoteSocketAddress());
        Closeables.closeQuietly(channel.socket());
        Closeables.closeQuietly(channel);
        key.attach(null);
        key.cancel();
    }

    private void write(SelectionKey key) throws IOException {
        Send response = (Send) key.attachment();
        SocketChannel socketChannel = channelFor(key);
        response.write(socketChannel);
        if (response.completed()) {
            key.attach(null);
            key.interestOps(SelectionKey.OP_READ);
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
            getSelector().wakeup();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel socketChannel = channelFor(key);
        Receive request;
        if (key.attachment() == null) {
            request = new JodisReceive(maxRequestSize);
            key.attach(request);
        } else {
            request = (Receive) key.attachment();
        }
        int read = request.read(socketChannel);
        if (read < 0) {
            close(key);
            // 表示暂时还未读完
        } else if (request.complete()) {
            Send maybeResponse = handle(request);
            key.attach(null);
            // if there is a response, send it, otherwise do nothing
            if (maybeResponse != null) {
                key.attach(maybeResponse);
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } else {
            // more reading to be done
            key.interestOps(SelectionKey.OP_READ);
            getSelector().wakeup();
            logger.info("reading request not been done {}", request);
        }
    }

    private Send handle(Receive request) {
        return requestHandler.handle(request);
    }

    private void configureNewConnections() throws ClosedChannelException {
        while (newConnections.size() > 0) {
            SocketChannel channel = newConnections.poll();
            logger.info("Listening to new connection from {}", channel.socket().getRemoteSocketAddress());
            channel.register(getSelector(), SelectionKey.OP_READ);
        }
    }
}
