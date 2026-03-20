package cn.abelib.jodis.remoting.netty;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ErrorResponse;
import cn.abelib.jodis.protocol.Response;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Netty服务器处理器，处理客户端请求
 * 
 * @Author: abel.huang
 * @Date: 2025-03-21
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = Logger.getLogger(getClass());
    private final JodisDb jodisDb;

    public NettyServerHandler(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String request = new String(bytes, StandardCharsets.UTF_8);

            // 处理换行符
            int len = request.length();
            if (len >= 1 && request.endsWith("\n")) {
                request = request.substring(0, len - 1);
            }
            if (len >= 2 && request.endsWith("\r")) {
                request = request.substring(0, request.length() - 1);
            }

            Response response;
            try {
                response = jodisDb.execute(request);
            } catch (IOException e) {
                response = ErrorResponse.errorCommon();
            }

            String respString = response.toRespString();
            byte[] responseBytes = ByteUtils.getBytesUTF8(respString);
            ByteBuf responseBuf = Unpooled.wrappedBuffer(responseBytes);
            ctx.writeAndFlush(responseBuf);
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage());
            String errorResp = ErrorResponse.errorCommon().toRespString();
            byte[] errorBytes = ByteUtils.getBytesUTF8(errorResp);
            ctx.writeAndFlush(Unpooled.wrappedBuffer(errorBytes));
        } finally {
            byteBuf.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Client disconnected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught: {}", cause.getMessage());
        ctx.close();
    }
}
