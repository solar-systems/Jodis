package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-08-04 22:18
 */
public class ThreadUtils {
    private ThreadUtils(){}

    public static Thread newThread(String name, Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        return thread;
    }
}
