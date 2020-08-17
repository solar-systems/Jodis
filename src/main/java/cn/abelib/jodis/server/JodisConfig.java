package cn.abelib.jodis.server;

import cn.abelib.jodis.utils.PropertiesUtils;
import cn.abelib.jodis.utils.StringUtils;

import java.util.Properties;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:11
 */
public class JodisConfig {
    private final Properties props;

    /**
     * 日志加载配置, 重写日志配置, 网络连接配置
     * @param props
     */
    public JodisConfig(Properties props) {
        this.props = props;
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

    public String getLogDir() {
        String logDir = PropertiesUtils.getString(props, "log.dir", "log/");
        if (StringUtils.isEmpty(logDir)) {
            throw new ConfigurationException(StringUtils.format("Invalid log directory {}, log directory must not be empty", logDir));
        }
        return logDir;
    }

    public String getLogJdb() {
        String logJdb = PropertiesUtils.getString(props, "log.jdb", "default.jdb");
        if (StringUtils.isEmpty(logJdb)) {
            throw new ConfigurationException(StringUtils.format("Invalid jdb log {}, jdb log file name must not be empty", logJdb));
        }
        return logJdb;
    }

    public String getLogWal() {
        String logWal =  PropertiesUtils.getString(props, "log.wal", "default.wal");
        if (StringUtils.isEmpty(logWal)) {
            throw new ConfigurationException(StringUtils.format("Invalid wal log {}, wal log file name must not be empty", logWal));
        }
        return logWal;
    }

    public int getRewriteSize() {
        int rewriteSize = PropertiesUtils.getInteger(props, "log.wal.rewrite.size", 64 * 1024 * 1024);
        if (rewriteSize < 64 * 1024 || rewriteSize > 1024 * 1024 * 1024) {
            throw new ConfigurationException(StringUtils.format("Invalid rewriteSize {}, rewriteSize must in [64KB, 1024MB]", rewriteSize));
        }
        return rewriteSize;
    }

    public int getReloadMode() {
        int reloadMode = PropertiesUtils.getInteger(props, "log.reload.mode", 2);
        if (reloadMode < 0 || reloadMode > 2) {
            throw new ConfigurationException(StringUtils.format("Invalid reloadMode {}, reloadMode must be a integer in [0, 1, 2]", reloadMode));
        }
        return reloadMode;
    }

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
}
