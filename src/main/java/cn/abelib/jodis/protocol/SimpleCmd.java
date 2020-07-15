package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public class SimpleCmd implements RespCmd {
    private String prefix = CmdConstant.SIMPLE_STRING_PREFIX;
    private String content;

    public SimpleCmd(String content) {
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
    public String toString() {
        return super.toString();
    }
}
