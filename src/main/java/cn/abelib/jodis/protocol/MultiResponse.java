package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:49
 */
public class MultiResponse implements Response{
    @Override
    public String toRespString() {
        return null;
    }

    @Override
    public boolean isError() {
        return false;
    }
}
