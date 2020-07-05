package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public class SimpleStringCommand implements RespCommand{
    private String prefix = RespConstant.SIMPLE_STRING_PREFIX;
    private String content;

    public SimpleStringCommand(String content) {
        this.content = content;
    }

    @Override
    public String prefix() {
        return this.prefix;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
