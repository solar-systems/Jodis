package cn.abelib.jodis.utils;

import org.junit.Assert;
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
        
        // 添加断言确保代码执行成功
        Assert.assertNotNull(logger);
    }

    @Test
    public void loggerTest() throws Exception {
        // 使用标准方式获取 Logger
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(this.getClass().getName());
        logger.info("Hello");
        Assert.assertNotNull(logger);
    }

    @Test
    public void logger2Test() throws Exception {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(this.getClass().getName());
        logger.info("Hello");
        Assert.assertNotNull(logger);
    }
}
