package cn.abelib.jodis.client;

import cn.abelib.jodis.utils.PropertiesUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Jodis 客户端工厂
 * 
 * @author abel.huang
 * @date 2026-03-21
 */
public class JodisClientFactory {
    
    /**
     * 创建默认配置的客户端
     * @return JodisClient
     */
    public static JodisClient createDefault() {
        return new JodisClient("localhost", 6059);
    }

    /**
     * 创建指定主机和端口的客户端
     * @param host 主机名
     * @param port 端口
     * @return JodisClient
     */
    public static JodisClient create(String host, int port) {
        return new JodisClient(host, port);
    }

    /**
     * 使用配置文件创建客户端
     * @param configPath 配置文件路径
     * @return JodisClient
     * @throws IOException
     */
    public static JodisClient createFromConfig(String configPath) throws IOException {
        Properties props = PropertiesUtils.loadProps(configPath);
        
        String host = props.getProperty("jodis.client.host", "localhost");
        int port = Integer.parseInt(props.getProperty("jodis.client.port", "6059"));
        int connectionTimeout = Integer.parseInt(props.getProperty("jodis.client.connection.timeout", "5000"));
        int soTimeout = Integer.parseInt(props.getProperty("jodis.client.so.timeout", "5000"));
        
        JodisClientConfig config = new JodisClientConfig(host, port);
        config.setConnectionTimeout(connectionTimeout);
        config.setSoTimeout(soTimeout);
        
        return new JodisClient(config);
    }

    /**
     * 使用 Properties 对象创建客户端
     * @param props 配置属性
     * @return JodisClient
     */
    public static JodisClient createFromProperties(Properties props) {
        String host = props.getProperty("jodis.client.host", "localhost");
        int port = Integer.parseInt(props.getProperty("jodis.client.port", "6059"));
        int connectionTimeout = Integer.parseInt(props.getProperty("jodis.client.connection.timeout", "5000"));
        int soTimeout = Integer.parseInt(props.getProperty("jodis.client.so.timeout", "5000"));
        
        JodisClientConfig config = new JodisClientConfig(host, port);
        config.setConnectionTimeout(connectionTimeout);
        config.setSoTimeout(soTimeout);
        
        return new JodisClient(config);
    }
}
