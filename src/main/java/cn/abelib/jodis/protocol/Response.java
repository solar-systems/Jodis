package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public interface Response {
    String toRespString();

    boolean isError();
}
