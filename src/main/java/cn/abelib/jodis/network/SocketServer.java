package cn.abelib.jodis.network;

import cn.abelib.jodis.network.jodis.JodisHandler;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.Closeables;
import cn.abelib.jodis.utils.Logger;
import cn.abelib.jodis.utils.ThreadUtils;

import java.io.Closeable;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:40
 */
public class SocketServer implements Closeable {
    private final Logger logger = Logger.getLogger(SocketServer.class);

    private RequestHandler requestHandler;
    private Processor processor;
    private Accepter accepter;
    private JodisConfig jodisConfig;

    public SocketServer(JodisHandler jodisHandler, JodisConfig jodisConfig) {
        this.jodisConfig = jodisConfig;
        this.requestHandler = jodisHandler;
        this.processor = new Processor(requestHandler);
        this.accepter = new Accepter(jodisConfig.getPort(), processor);
    }

    public void startup() throws InterruptedException {
        ThreadUtils.newThread("jodis-processor", processor, false).start();
        ThreadUtils.newThread("jodis-acceptor", accepter, false).start();
        accepter.awaitStartup();
    }

    @Override
    public void close() {
        Closeables.closeQuietly(accepter);
        Closeables.closeQuietly(processor);
    }
}
