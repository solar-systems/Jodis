package cn.abelib.jodis.client;

/**
 * Jodis 客户端配置
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClientConfig {
    private String host = "localhost";
    private int port = 6059;
    private int connectionTimeout = 5000; // 5 秒
    private int soTimeout = 5000; // 读取超时时间

    public JodisClientConfig() {
    }

    public JodisClientConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
}
