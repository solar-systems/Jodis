package cn.abelib.jodis.utils;

import com.google.common.base.Throwables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @Author: abel.huang
 * @Date: 2020-08-02 15:01
 */
public class Logger {
    private java.util.logging.Logger logger;

    /**
     *  todo callerClass not work
     * private constructor
     * @param tClass
     */
    private Logger(Class tClass) {
        try {
            Class clazz = Class.forName("java.util.logging.Logger");
            Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class,
                    String.class,
                    Class.class,
                    LogManager.class,
                    boolean.class});
            constructor.setAccessible(true);
            this.logger = (java.util.logging.Logger) constructor.newInstance( tClass.getName(),
                    null,
                    tClass,
                    LogManager.getLogManager(),
                    false);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException ignored) {
            new Logger(tClass.getName());
        }
    }

    private Logger(String name) {
        this.logger = java.util.logging.Logger.getLogger(name);
    }
    public static Logger getLogger(Class clazz) {
        return new Logger(clazz.getName());
    }

    /**
     * LEVEL.INFO
     * @param msg
     */
    public void info(String msg) {
        this.logger.info(msg);
    }

    public void info(Throwable t) {
        this.info(Throwables.getStackTraceAsString(t));
    }

    public void info(String msgFormat, Object... args) {
       this.info(String.format(msgFormat.replaceAll("\\{\\}", "%s"), args));
    }

    /**
     * LEVEL.WARN
     * @param msg
     */
    public void warn(String msg) {
        this.logger.log(Level.SEVERE, msg);
    }

    public void warn(Throwable t) {
        this.warn(Throwables.getStackTraceAsString(t));
    }

    public void warn(String msgFormat, Object... args) {
        this.warn(String.format(msgFormat.replaceAll("\\{\\}", "%s"), args));
    }

    /**
     * LEVEL.SEVERE
     * @param msg
     */
    public void error(String msg) {
        this.logger.log(Level.SEVERE, msg);
    }

    public void error(Throwable t) {
        this.error(Throwables.getStackTraceAsString(t));
    }

    public void error(String msgFormat, Object... args) {
        this.error(String.format(msgFormat.replaceAll("\\{\\}", "%s"), args));
    }
}
