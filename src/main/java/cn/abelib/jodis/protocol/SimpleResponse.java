package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public class SimpleResponse implements Response {
    private String prefix = ProtocolConstant.SIMPLE_STRING_PREFIX;
    private String content;

    public SimpleResponse(String content) {
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

    public static SimpleResponse ok() {
        return new SimpleResponse("OK");
    }

    public static SimpleResponse result(String content) {
        return new SimpleResponse(content);
    }
}