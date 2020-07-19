package cn.abelib.jodis.protocol;

/**
 * @author abel.huang
 * @date 2020/6/30 18:48
 */
public interface RespCmd {
    String prefix();

    String toRespString();

    boolean isError();
}
