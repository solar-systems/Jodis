package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

/**
 * @author abel.huang
 * @date 2020/6/30 18:53
 * 错误响应
 */
public class ErrorResponse implements Response {
    private String content;

    public ErrorResponse(String content) {
        this.content = content;
    }

    /**
     * -ERR ERROR_PHRASE
     * @return
     */
    @Override
    public String toRespString() {
        return ProtocolConstant.ERROR_PREFIX + this.content + StringUtils.CLRF;
    }

    @Override
    public boolean isError() {
        return true;
    }

    /**
     * 通用错误
     * @return
     */
    public static ErrorResponse errorCommon() {
        String content = "execute error";
        return new ErrorResponse(content);
    }

    public static ErrorResponse error(String msg) {
        return new ErrorResponse(msg);
    }

    /**
     *  todo 需要增加更多的描述信息
     * 语法错误
     * @return
     */
    public static ErrorResponse errorSyntax() {
        String content = "syntax error";
        return new ErrorResponse(content);
    }

    public static ErrorResponse errorInvalidNumber() {
        String content = "invalid number";
        return new ErrorResponse(content);
    }

    /**
     * 未知命令
     * @param command
     * @return
     */
    public static ErrorResponse errorUnknownCmd(String command) {
        String content = "unknown command '{}'";
        return new ErrorResponse(StringUtils.format(content, command));
    }

    /**
     * 参数数量错误
     * @param command
     * @param expect
     * @param actual
     * @return
     */
    public static ErrorResponse errorArgsNum(String command, int expect, int actual) {
        String content = "wrong number of arguments for '{}' command, require {}, but found {}";
        return new ErrorResponse(StringUtils.format(content, command, expect, actual));
    }

    /**
     * 参数数量错误
     * @param command
     * @return
     */
    public static ErrorResponse errorArgsNum(String command) {
        String content = "wrong number of arguments for '{}' command";
        return new ErrorResponse(StringUtils.format(content, command));
    }
}
