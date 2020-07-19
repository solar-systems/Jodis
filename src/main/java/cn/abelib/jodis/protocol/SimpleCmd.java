package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

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
        return this.prefix + content + StringUtils.CLRF;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static SimpleCmd ok() {
        return new SimpleCmd("OK");
    }

    public static SimpleCmd result(String content) {
        return new SimpleCmd(content);
    }
}