package cn.abelib.jodis.protocol;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.remoting.Receive;
import cn.abelib.jodis.remoting.RequestHandler;
import cn.abelib.jodis.remoting.Send;
import cn.abelib.jodis.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:43
 */
public class JodisHandler implements RequestHandler {
    public JodisDb jodisDb;
    private Logger logger = Logger.getLogger(JodisHandler.class);

    public JodisHandler(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }
    @Override
    public Send handle(Receive receive) {
        ByteBuffer buffer = receive.buffer();
        buffer.flip();

        // 处理请求
        // 检查是否有足够的数据读取长度前缀（4 字节）
        if (buffer.remaining() < 4) {
            return new JodisSend(ErrorResponse.errorCommon());
        }

        // 读取 4 字节长度前缀
        int length = buffer.getInt();

        // 检查长度是否合理
        if (length <= 0 || length > buffer.remaining()) {
            return new JodisSend(ErrorResponse.errorCommon());
        }

        // 读取实际的请求数据
        byte[] requestData = new byte[length];
        buffer.get(requestData);
        String request = new String(requestData, java.nio.charset.StandardCharsets.UTF_8);

        // 移除末尾的 \r\n（RESP 协议格式）
        if (request.endsWith("\r\n")) {
            request = request.substring(0, request.length() - 2);
        } else if (request.endsWith("\n")) {
            request = request.substring(0, request.length() - 1);
        }

        Response response;
        try {
            response = jodisDb.execute(request);
        } catch (IOException e) {
            response = ErrorResponse.errorCommon();
        } catch (Exception e) {
            response = ErrorResponse.error("Internal error: " + e.getMessage());
        }

        return new JodisSend(response);
    }
}
