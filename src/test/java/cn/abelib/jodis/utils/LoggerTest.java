package cn.abelib.jodis.utils;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.logging.LogManager;

/**
 * @Author: abel.huang
 * @Date: 2020-08-02 15:16
 */
public class LoggerTest {

    Logger logger = Logger.getLogger(LoggerTest.class);

    @Test
    public void logTest() {
        logger.info("info");
        logger.info("info {} {}", "info1", "info2");

        logger.warn("warn");
        logger.warn("warn {} {}", "warn1", "warn2");

        logger.error("error");
        logger.error("error {} {}", "error1", "erro2");
    }

    @Test
    public void loggerTest() throws Exception {
        Class tClass = this.getClass();
        Class clazz = Class.forName("java.util.logging.Logger");
        Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class,
                String.class,
                Class.class,
                LogManager.class,
                boolean.class});
        constructor.setAccessible(true);
        java.util.logging.Logger logger = (java.util.logging.Logger) constructor.newInstance( tClass.getName(),
                null,
                tClass,
                LogManager.getLogManager(),
                false);
       // logger.addHandler();
        logger.info("Hello");
    }

    @Test
    public void logger2Test() throws Exception {

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(this.getClass().getName());
        logger.info("Hello");
    }
}
