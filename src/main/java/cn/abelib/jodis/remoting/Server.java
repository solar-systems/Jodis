package cn.abelib.jodis.remoting;

import java.io.Closeable;

/**
 * 服务器接口抽象，定义网络层服务器的通用行为
 * 
 * @Author: abel.huang
 * @Date: 2025-03-21
 */
public interface Server extends Closeable {

    /**
     * 启动服务器
     * @throws InterruptedException 如果启动被中断
     */
    void startup() throws InterruptedException;

    /**
     * 等待服务器启动完成
     * @throws InterruptedException 如果等待被中断
     */
    void awaitStartup() throws InterruptedException;
}
