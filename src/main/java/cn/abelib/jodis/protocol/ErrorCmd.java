package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:53
 */
public class ErrorCmd implements RespCmd{
    private String prefix = CmdConstant.ERROR_PREFIX;
    private String content;

    public ErrorCmd(String content) {
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

    public static ErrorCmd error(String msg) {
        return new ErrorCmd(msg);
    }

    public static ErrorCmd errorSyntax() {
        String content = "syntax error";
        return new ErrorCmd(content);
    }

    public static ErrorCmd errorArgsNum(String cmd) {
        String content = "wrong number of arguments for '" + cmd + "' command";
        return new ErrorCmd(content);
    }
}
