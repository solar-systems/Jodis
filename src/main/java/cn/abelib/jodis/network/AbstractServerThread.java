package cn.abelib.jodis.network;

import cn.abelib.jodis.utils.Closeables;
import cn.abelib.jodis.utils.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: abel.huang
 * @Date: 2020-08-02 18:58
 */
public abstract class AbstractServerThread implements Runnable, Closeable {
    private Selector selector;
    protected final CountDownLatch startupLatch = new CountDownLatch(1);
    protected final CountDownLatch shutdownLatch = new CountDownLatch(1);
    protected final AtomicBoolean alive = new AtomicBoolean(false);

    final Logger logger = Logger.getLogger(getClass());

    /**
     * @return the selector
     */
    public Selector getSelector() {
        if (selector == null) {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return selector;
    }

    protected void closeSelector() {
        Closeables.closeQuietly(selector);
    }

    @Override
    public void close() throws IOException {
        alive.set(false);
        selector.wakeup();
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    protected void startupComplete() {
        alive.set(true);
        startupLatch.countDown();
    }

    protected void shutdownComplete() {
        shutdownLatch.countDown();
    }

    protected boolean isRunning() {
        return alive.get();
    }

    public void awaitStartup() throws InterruptedException {
        startupLatch.await();
    }
}
