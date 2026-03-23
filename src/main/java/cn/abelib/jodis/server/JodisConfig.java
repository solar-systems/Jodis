package cn.abelib.jodis.server;

import cn.abelib.jodis.remoting.ServerFactory;
import cn.abelib.jodis.store.StoreConfig;
import cn.abelib.jodis.utils.PropertiesUtils;
import cn.abelib.jodis.utils.StringUtils;

import java.util.Properties;

/**
 * Jodis 服务器配置
 *
 * @Author: abel.huang
 * @Date: 2020-07-02 23:11
 */
public class JodisConfig {
    private final Properties props;
    private final StoreConfig storeConfig;

    /**
     * 日志加载配置, 重写日志配置, 网络连接配置
     * @param props
     */
    public JodisConfig(Properties props) {
        this.props = props;
        this.storeConfig = new StoreConfig(props);
    }

    /**
     * port must in [1025, 65535]
     * @return
     */
    public int getPort() {
        int port = PropertiesUtils.getInteger(props, "jodis.port", 6059);
        if (port < 1025 || port > 65535) {
            throw new ConfigurationException(StringUtils.format("Invalid port {}, port must in [1025, 65535]", port));
        }
        return port;
    }

    /**
     * 获取存储配置
     */
    public StoreConfig getStoreConfig() {
        return storeConfig;
    }

    // ==================== 委托给 StoreConfig 的方法 ====================

    public String getLogDir() {
        return storeConfig.getLogDir();
    }

    public String getLogJdb() {
        return storeConfig.getJdbFile();
    }

    public String getLogWal() {
        return storeConfig.getWalFile();
    }

    public int getRewriteSize() {
        return storeConfig.getRewriteSize();
    }

    public int getReloadMode() {
        return storeConfig.getReloadMode();
    }

    // ==================== 服务器配置 ====================

    public int getMaxRequestSize() {
        int maxRequestSize = PropertiesUtils.getInteger(props, "server.max.request", 1024);
        if (maxRequestSize < 1024 || maxRequestSize > 1024 * 8) {
            throw new ConfigurationException(StringUtils.format("Invalid maxRequestSize {}, maxRequestSize must in [1024, 8192]", maxRequestSize));
        }
        return maxRequestSize;
    }

    public int getMaxConcurrency() {
        int concurrency = PropertiesUtils.getInteger(props, "server.max.concurrency", 64);
        if (concurrency < 8 || concurrency > 1024) {
            throw new ConfigurationException(StringUtils.format("Invalid concurrency {}, concurrency must in [8, 1024]", concurrency));
        }
        return concurrency;
    }

    /**
     * 获取服务器类型配置
     * 支持: nio (Java NIO Socket) 或 netty (Netty)
     * @return 服务器类型
     */
    public ServerFactory.ServerType getServerType() {
        String serverType = PropertiesUtils.getString(props, "server.type", "nio");
        return ServerFactory.ServerType.fromString(serverType);
    }
}
