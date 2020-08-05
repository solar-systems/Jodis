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
            throw new ConfigrationException(String.format("Invalid port {}, port must in [1025, 65535]", port));
        }
        return PropertiesUtils.getInteger(props, "jodis.port", 6059);
    }

    public String getLogDir() {
        String logDir = PropertiesUtils.getString(props, "log.dir", "log/");
        if (StringUtils.isEmpty(logDir)) {
            throw new ConfigrationException(String.format("Invalid log directory {}, log directory must not be empty", logDir));
        }
        return logDir;
    }

    public String getLogJdb() {
        String logJdb = PropertiesUtils.getString(props, "log.jdb", "default.jdb");
        if (StringUtils.isEmpty(logJdb)) {
            throw new ConfigrationException(String.format("Invalid jdb log {}, jdb log file name must not be empty", logJdb));
        }
        return logJdb;
    }

    public String getLogWal() {
        String logWal =  PropertiesUtils.getString(props, "log.wal", "default.wal");
        if (StringUtils.isEmpty(logWal)) {
            throw new ConfigrationException(String.format("Invalid wal log {}, wal log file name must not be empty", logWal));
        }
        return logWal;
    }
}
