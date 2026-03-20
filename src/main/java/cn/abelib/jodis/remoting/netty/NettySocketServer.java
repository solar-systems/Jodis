package cn.abelib.jodis.remoting.netty;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.remoting.Server;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.CountDownLatch;

/**
 * 基于Netty的服务器实现
 * 
 * @Author: abel.huang
 * @Date: 2025-03-21
 */
public class NettySocketServer implements Server {
    private final Logger logger = Logger.getLogger(getClass());

    private final JodisConfig jodisConfig;
    private final JodisDb jodisDb;
    private final CountDownLatch startupLatch = new CountDownLatch(1);
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettySocketServer(JodisDb jodisDb, JodisConfig jodisConfig) {
        this.jodisDb = jodisDb;
        this.jodisConfig = jodisConfig;
    }

    @Override
    public void startup() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编解码器，使用基于长度的帧解码器处理RESP协议
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    jodisConfig.getMaxRequestSize() * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new NettyServerHandler(jodisDb));
                        }
                    });

            ChannelFuture future = bootstrap.bind(jodisConfig.getPort()).sync();
            serverChannel = future.channel();
            
            logger.info("Netty server started on port: {}", jodisConfig.getPort());
            startupLatch.countDown();

            // 等待服务器通道关闭
            serverChannel.closeFuture().sync();
        } finally {
            shutdownLatch.countDown();
        }
    }

    @Override
    public void awaitStartup() throws InterruptedException {
        startupLatch.await();
    }

    @Override
    public void close() {
        logger.info("Shutting down Netty server...");
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        logger.info("Netty server shutdown completed");
    }
}
