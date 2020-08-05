package cn.abelib.jodis.network;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:47
 */
public interface RequestHandler {

    Send handle(Receive receive);
}
