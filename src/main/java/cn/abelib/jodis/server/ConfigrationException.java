package cn.abelib.jodis.server;

/**
 * @Author: abel.huang
 * @Date: 2020-08-01 17:43
 *  配置异常类
 */
public class ConfigrationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConfigrationException() {
    }

    public ConfigrationException(String message) {
        super(message);
    }

    public ConfigrationException(Throwable cause) {
        super(cause);
    }

    public ConfigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
