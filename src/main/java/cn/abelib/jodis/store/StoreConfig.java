package cn.abelib.jodis.store;

import cn.abelib.jodis.server.ConfigurationException;
import cn.abelib.jodis.utils.PropertiesUtils;
import cn.abelib.jodis.utils.StringUtils;

import java.util.Properties;

/**
 * 存储配置类
 * 统一管理 JDB 和 WAL 的存储相关配置
 *
 * @author abel.huang
 * @version 1.0
 * @date 2022/6/26 17:55
 */
public class StoreConfig {

    // ==================== 默认值常量 ====================

    /**
     * 默认 JDB 文件大小: 1GB
     */
    public static final int DEFAULT_JDB_FILE_SIZE = 1024 * 1024 * 1024;

    /**
     * 最小 JDB 文件大小: 1MB
     */
    public static final int MIN_JDB_FILE_SIZE = 1024 * 1024;

    /**
     * 最大 JDB 文件大小: 1GB (int 最大值约 2.1GB，使用 1GB 避免溢出)
     */
    public static final int MAX_JDB_FILE_SIZE = 1024 * 1024 * 1024;

    /**
     * 默认日志目录
     */
    public static final String DEFAULT_LOG_DIR = "log/";

    /**
     * 默认 JDB 文件名
     */
    public static final String DEFAULT_JDB_FILE = "default.jdb";

    /**
     * 默认 WAL 文件名
     */
    public static final String DEFAULT_WAL_FILE = "default.wal";

    /**
     * 默认 Rewrite 大小阈值: 64MB
     */
    public static final int DEFAULT_REWRITE_SIZE = 64 * 1024 * 1024;

    /**
     * 最小 Rewrite 大小: 1MB
     */
    public static final int MIN_REWRITE_SIZE = 1024 * 1024;

    /**
     * 最大 Rewrite 大小: 1GB
     */
    public static final int MAX_REWRITE_SIZE = 1024 * 1024 * 1024;

    /**
     * 默认加载模式: 混合模式
     */
    public static final int DEFAULT_RELOAD_MODE = 2;

    /**
     * 默认自动保存间隔: 60秒
     */
    public static final int DEFAULT_AUTO_SAVE_INTERVAL = 60;

    /**
     * 默认刷盘页数阈值
     */
    public static final int DEFAULT_FLUSH_PAGES = 0;

    // ==================== 配置属性 ====================

    private final Properties props;

    /**
     * 日志目录
     */
    private String logDir;

    /**
     * JDB 文件名
     */
    private String jdbFile;

    /**
     * WAL 文件名
     */
    private String walFile;

    /**
     * JDB 文件大小
     */
    private int jdbFileSize;

    /**
     * Rewrite 大小阈值
     */
    private int rewriteSize;

    /**
     * 加载模式: 0=WAL, 1=JDB, 2=混合
     */
    private int reloadMode;

    /**
     * 自动保存间隔（秒）
     */
    private int autoSaveInterval;

    /**
     * 是否启用自动保存
     */
    private boolean autoSaveEnabled;

    /**
     * 是否启用 WAL
     */
    private boolean walEnabled;

    /**
     * 刷盘页数阈值
     */
    private int flushPages;

    // ==================== 构造方法 ====================

    public StoreConfig() {
        this(new Properties());
    }

    public StoreConfig(Properties props) {
        this.props = props != null ? props : new Properties();
        init();
    }

    /**
     * 初始化配置
     */
    private void init() {
        this.logDir = loadLogDir();
        this.jdbFile = loadJdbFile();
        this.walFile = loadWalFile();
        this.jdbFileSize = loadJdbFileSize();
        this.rewriteSize = loadRewriteSize();
        this.reloadMode = loadReloadMode();
        this.autoSaveInterval = loadAutoSaveInterval();
        this.autoSaveEnabled = loadAutoSaveEnabled();
        this.walEnabled = loadWalEnabled();
        this.flushPages = loadFlushPages();
    }

    // ==================== 配置加载方法 ====================

    private String loadLogDir() {
        String dir = PropertiesUtils.getString(props, "log.dir", DEFAULT_LOG_DIR);
        if (StringUtils.isEmpty(dir)) {
            throw new ConfigurationException("Log directory must not be empty");
        }
        return dir;
    }

    private String loadJdbFile() {
        String file = PropertiesUtils.getString(props, "log.jdb", DEFAULT_JDB_FILE);
        if (StringUtils.isEmpty(file)) {
            throw new ConfigurationException("JDB file name must not be empty");
        }
        return file;
    }

    private String loadWalFile() {
        String file = PropertiesUtils.getString(props, "log.wal", DEFAULT_WAL_FILE);
        if (StringUtils.isEmpty(file)) {
            throw new ConfigurationException("WAL file name must not be empty");
        }
        return file;
    }

    private int loadJdbFileSize() {
        int size = PropertiesUtils.getInteger(props, "store.jdb.file.size", DEFAULT_JDB_FILE_SIZE);
        if (size < MIN_JDB_FILE_SIZE || size > MAX_JDB_FILE_SIZE) {
            throw new ConfigurationException(
                    StringUtils.format("Invalid JDB file size {}, must be between {}MB and {}GB",
                            size, MIN_JDB_FILE_SIZE / 1024 / 1024, MAX_JDB_FILE_SIZE / 1024 / 1024 / 1024));
        }
        return size;
    }

    private int loadRewriteSize() {
        int size = PropertiesUtils.getInteger(props, "log.wal.rewrite.size", DEFAULT_REWRITE_SIZE);
        if (size < MIN_REWRITE_SIZE || size > MAX_REWRITE_SIZE) {
            throw new ConfigurationException(
                    StringUtils.format("Invalid rewrite size {}, must be between {}MB and {}MB",
                            size, MIN_REWRITE_SIZE / 1024 / 1024, MAX_REWRITE_SIZE / 1024 / 1024));
        }
        return size;
    }

    private int loadReloadMode() {
        int mode = PropertiesUtils.getInteger(props, "log.reload.mode", DEFAULT_RELOAD_MODE);
        if (mode < 0 || mode > 2) {
            throw new ConfigurationException(
                    StringUtils.format("Invalid reload mode {}, must be 0(WAL), 1(JDB) or 2(MIX)", mode));
        }
        return mode;
    }

    private int loadAutoSaveInterval() {
        int interval = PropertiesUtils.getInteger(props, "store.auto.save.interval", DEFAULT_AUTO_SAVE_INTERVAL);
        if (interval < 0) {
            throw new ConfigurationException("Auto save interval must be >= 0");
        }
        return interval;
    }

    private boolean loadAutoSaveEnabled() {
        return PropertiesUtils.getInteger(props, "store.auto.save.enabled", 1) == 1;
    }

    private boolean loadWalEnabled() {
        return PropertiesUtils.getInteger(props, "store.wal.enabled", 1) == 1;
    }

    private int loadFlushPages() {
        return PropertiesUtils.getInteger(props, "store.flush.pages", DEFAULT_FLUSH_PAGES);
    }

    // ==================== Getter 方法 ====================

    public String getLogDir() {
        return logDir;
    }

    public String getJdbFile() {
        return jdbFile;
    }

    public String getWalFile() {
        return walFile;
    }

    public int getJdbFileSize() {
        return jdbFileSize;
    }

    public int getRewriteSize() {
        return rewriteSize;
    }

    public int getReloadMode() {
        return reloadMode;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public boolean isWalEnabled() {
        return walEnabled;
    }

    public int getFlushPages() {
        return flushPages;
    }

    /**
     * 获取完整的 JDB 文件路径
     */
    public String getJdbFilePath() {
        return logDir + (logDir.endsWith("/") ? "" : "/") + jdbFile;
    }

    /**
     * 获取完整的 WAL 文件路径
     */
    public String getWalFilePath() {
        return logDir + (logDir.endsWith("/") ? "" : "/") + walFile;
    }

    // ==================== Setter 方法（用于程序化配置） ====================

    public void setLogDir(String logDir) {
        if (StringUtils.isEmpty(logDir)) {
            throw new ConfigurationException("Log directory must not be empty");
        }
        this.logDir = logDir;
    }

    public void setJdbFile(String jdbFile) {
        if (StringUtils.isEmpty(jdbFile)) {
            throw new ConfigurationException("JDB file name must not be empty");
        }
        this.jdbFile = jdbFile;
    }

    public void setWalFile(String walFile) {
        if (StringUtils.isEmpty(walFile)) {
            throw new ConfigurationException("WAL file name must not be empty");
        }
        this.walFile = walFile;
    }

    public void setJdbFileSize(int jdbFileSize) {
        if (jdbFileSize < MIN_JDB_FILE_SIZE || jdbFileSize > MAX_JDB_FILE_SIZE) {
            throw new ConfigurationException("Invalid JDB file size");
        }
        this.jdbFileSize = jdbFileSize;
    }

    public void setRewriteSize(int rewriteSize) {
        if (rewriteSize < MIN_REWRITE_SIZE || rewriteSize > MAX_REWRITE_SIZE) {
            throw new ConfigurationException("Invalid rewrite size");
        }
        this.rewriteSize = rewriteSize;
    }

    public void setReloadMode(int reloadMode) {
        if (reloadMode < 0 || reloadMode > 2) {
            throw new ConfigurationException("Invalid reload mode");
        }
        this.reloadMode = reloadMode;
    }

    public void setAutoSaveInterval(int autoSaveInterval) {
        if (autoSaveInterval < 0) {
            throw new ConfigurationException("Auto save interval must be >= 0");
        }
        this.autoSaveInterval = autoSaveInterval;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public void setWalEnabled(boolean walEnabled) {
        this.walEnabled = walEnabled;
    }

    // ==================== 工具方法 ====================

    /**
     * 是否使用 JDB 加载
     */
    public boolean useJdbReload() {
        return reloadMode == 1 || reloadMode == 2;
    }

    /**
     * 是否使用 WAL 加载
     */
    public boolean useWalReload() {
        return reloadMode == 0 || reloadMode == 2;
    }

    @Override
    public String toString() {
        return "StoreConfig{" +
                "logDir='" + logDir + '\'' +
                ", jdbFile='" + jdbFile + '\'' +
                ", walFile='" + walFile + '\'' +
                ", jdbFileSize=" + jdbFileSize / 1024 / 1024 + "MB" +
                ", rewriteSize=" + rewriteSize / 1024 / 1024 + "MB" +
                ", reloadMode=" + reloadMode +
                ", autoSaveInterval=" + autoSaveInterval + "s" +
                ", autoSaveEnabled=" + autoSaveEnabled +
                ", walEnabled=" + walEnabled +
                '}';
    }
}
