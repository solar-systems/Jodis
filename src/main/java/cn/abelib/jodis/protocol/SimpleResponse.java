package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public class SimpleResponse implements Response {
    private String content;

    public SimpleResponse(String content) {
        this.content = content;
    }

    public static SimpleResponse simpleResponse(String content) {
        return new SimpleResponse(content);
    }

    public static SimpleResponse ok() {
        return new SimpleResponse("OK");
    }

    @Override
    public String toRespString() {
        return ProtocolConstant.SIMPLE_STRING_PREFIX + content + StringUtils.CLRF;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public String toString() {
        return this.toRespString();
    }
}