package cn.abelib.jodis.server;

/**
 * @Author: abel.huang
 * @Date: 2020-08-01 17:43
 *  配置异常类
 */
public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
