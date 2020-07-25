package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:53
 */
public class ErrorResponse implements Response {
    private String prefix = ProtocolConstant.ERROR_PREFIX;
    private String content;

    public ErrorResponse(String content) {
        this.content = content;
    }

    @Override
    public String prefix() {
        return this.prefix;
    }

    @Override
    public String toRespString() {
        return null;
    }

    @Override
    public boolean isError() {
        return true;
    }

    public static ErrorResponse error(String msg) {
        return new ErrorResponse(msg);
    }

    public static ErrorResponse errorSyntax() {
        String content = "syntax error";
        return new ErrorResponse(content);
    }

    public static ErrorResponse errorArgsNum(String cmd) {
        String content = "wrong number of arguments for '" + cmd + "' command";
        return new ErrorResponse(content);
    }
}
