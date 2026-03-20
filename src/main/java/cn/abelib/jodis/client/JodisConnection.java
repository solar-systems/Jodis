package cn.abelib.jodis.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Jodis 客户端连接
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisConnection implements AutoCloseable {
    private Socket socket;
    private String host;
    private int port;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean connected = false;

    public JodisConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到 Jodis 服务器
     * @throws IOException
     */
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(5000); // 5 秒超时
        
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        
        connected = true;
    }

    /**
     * 检查是否已连接
     * @return
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 发送命令并获取响应
     * @param command 命令（大写）
     * @param args 参数列表
     * @return 响应字符串
     * @throws IOException
     */
    public String sendCommand(String command, String... args) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to server");
        }

        // 构建 RESP 协议格式的请求
        StringBuilder request = new StringBuilder();
        List<String> allArgs = new ArrayList<>();
        allArgs.add(command);
        if (args != null) {
            for (String arg : args) {
                allArgs.add(arg);
            }
        }

        // *<参数数量>\r\n
        request.append("*").append(allArgs.size()).append("\r\n");
        
        // 每个参数：$<长度>\r\n<值>\r\n
        for (String arg : allArgs) {
            byte[] argBytes = arg.getBytes(StandardCharsets.UTF_8);
            request.append("$").append(argBytes.length).append("\r\n");
            request.append(arg).append("\r\n");
        }

        // 发送请求
        String requestStr = request.toString();
        byte[] requestBytes = requestStr.getBytes(StandardCharsets.UTF_8);
        
        // 写入 4 字节长度前缀 (大端模式)
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(requestBytes.length);
        outputStream.write(lengthBuffer.array());
        outputStream.write(requestBytes);
        outputStream.flush();

        // 读取响应
        return readResponse();
    }

    /**
     * 读取响应
     * @return 响应内容
     * @throws IOException
     */
    private String readResponse() throws IOException {
        // 先读取 4 字节长度前缀
        byte[] lengthBuffer = new byte[4];
        int bytesRead = inputStream.read(lengthBuffer);
        if (bytesRead != 4) {
            throw new IOException("Connection closed by server or no response. Please check if Jodis server is running.");
        }
        
        // 解析长度（大端模式）
        ByteBuffer buffer = ByteBuffer.wrap(lengthBuffer);
        int responseLength = buffer.getInt();
        
        // 读取响应数据
        byte[] responseDataBytes = new byte[responseLength];
        int totalRead = 0;
        while (totalRead < responseLength) {
            int read = inputStream.read(responseDataBytes, totalRead, responseLength - totalRead);
            if (read == -1) {
                throw new IOException("End of stream before reading complete response");
            }
            totalRead += read;
        }
        
        String responseData = new String(responseDataBytes, StandardCharsets.UTF_8);
        
        // 解析 RESP 响应
        if (responseData.isEmpty()) {
            throw new IOException("Empty response");
        }
        
        char type = responseData.charAt(0);
        
        switch (type) {
            case '+': // Simple String - 去除末尾的 \r\n
                return responseData.substring(1).trim();
            case '-': // Error
                throw new IOException("Server error: " + responseData.substring(1).trim());
            case ':': // Integer - 去除末尾的 \r\n
                return responseData.substring(1).trim();
            case '$': // Bulk String
                return parseBulkString(responseData);
            case '*': // Array
                return parseArray(responseData);
            default:
                throw new IOException("Unknown response type: " + type);
        }
    }

    /**
     * 解析批量字符串
     */
    private String parseBulkString(String data) {
        // 找到第一个 \r\n
        int firstCrLf = data.indexOf("\r\n");
        if (firstCrLf == -1) {
            throw new RuntimeException("Invalid bulk string format");
        }
        
        // 解析长度
        int length = Integer.parseInt(data.substring(1, firstCrLf));
        if (length == -1) {
            return null; // null bulk string
        }
        
        // 提取实际数据
        String value = data.substring(firstCrLf + 2, firstCrLf + 2 + length);
        return value;
    }

    /**
     * 解析数组
     */
    private String parseArray(String data) {
        // 简单实现，返回原始数据
        return data;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        connected = false;
    }
}
