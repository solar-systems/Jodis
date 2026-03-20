package cn.abelib.jodis.server;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.remoting.Server;
import cn.abelib.jodis.remoting.ServerFactory;
import cn.abelib.jodis.utils.Logger;
import com.google.common.base.Stopwatch;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: abel.huang
 * @Date: 2020-08-01 18:20
 */
public class JodisServer implements Closeable {
    private Logger logger = Logger.getLogger(JodisServer.class);

    private JodisConfig jodisConfig;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private Server server;
    private JodisDb jodisDb;

    public JodisServer(JodisConfig jodisConfig) {
        this.jodisConfig = jodisConfig;
    }

    public void startup() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            jodisDb = new JodisDb(jodisConfig);
            // 使用工厂创建服务器，支持NIO和Netty两种实现
            server = ServerFactory.createServer(jodisDb, jodisConfig);
            server.startup();
            logger.info("Jodis server started cost {}", stopwatch.stop().toString());
        } catch (Exception e) {
            logger.error("========================================");
            logger.error("Fatal error {} during startup.", e);
            logger.error("========================================");
        }
    }

    @Override
    public void close() {
        boolean canShutdown = isShuttingDown.compareAndSet(false, true);
        if (!canShutdown) {
            return;//CLOSED
        }

        logger.info("Shutting down Jodis server...");
        try {
            if (server != null) {
                server.close();
            }

        } catch (Exception e) {
            logger.error(e);
        }
        shutdownLatch.countDown();
        logger.info("Shutdown Jodis server completed");
    }

    public void awaitShutdown() {
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            logger.warn(e);
        }
    }
}
