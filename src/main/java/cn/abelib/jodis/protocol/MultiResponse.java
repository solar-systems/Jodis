package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:49
 * todo 针对多行字符串的支持(目前对于多行解析可能存在问题)
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
