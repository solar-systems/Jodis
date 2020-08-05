package cn.abelib.jodis.server;

/**
 * @Author: abel.huang
 * @Date: 2020-08-06 00:06
 */
public class JodisException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public JodisException() {
        super();
    }

    public JodisException(String message, Throwable cause) {
        super(message, cause);
    }

    public JodisException(String message) {
        super(message);
    }

    public JodisException(Throwable cause) {
        super(cause);
    }
}
