package cn.abelib.jodis;

import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.server.JodisServer;
import cn.abelib.jodis.utils.Logger;
import cn.abelib.jodis.utils.PropertiesUtils;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:51
 * Independent deployment jodis server
 * Java Object Dictionary Server
 */
public class Jodis implements Closeable {
    Logger log = Logger.getLogger(Jodis.class);

    /**
     * 关闭钩子
     */
    private volatile Thread shutdownHook;

    private JodisServer jodisServer;

    public Jodis() {}

    public void start(String propsFileName) {
        Path path = Paths.get(propsFileName);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error( "ERROR: Jodis config file not exist => '{}', copy one from 'conf/jodis.properties' first.",
                    path.toAbsolutePath().toString());
            System.exit(-1);
        }

        start(PropertiesUtils.loadProps(propsFileName));
    }

    public void start(Properties mainProperties) {
        final JodisConfig config = new JodisConfig(mainProperties);
        start(config);
    }

    public void start(JodisConfig config) {
        jodisServer = new JodisServer(config);

        // todo Executor
        shutdownHook = new Thread(() -> {
            jodisServer.close();
            jodisServer.awaitShutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        jodisServer.startup();
    }

    public void awaitShutdown() {
        if (jodisServer != null) {
            jodisServer.awaitShutdown();
        }
    }

    @Override
    public void close() {
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
                //ignore shutting down status
            }
            shutdownHook.run();
            shutdownHook = null;
        }
    }

    public static void main(String[] args) {
        Jodis jodis = new Jodis();
        jodis.start("conf/jodis.properties");
        jodis.awaitShutdown();
        jodis.close();
    }
}
