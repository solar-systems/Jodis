package cn.abelib.jodis.network;

/**
 * @Author: abel.huang
 * @Date: 2020-07-30 22:47
 */
public interface RequestHandler {

    /**
     * Handle request and send response
     * @param receive
     * @return
     */
    Send handle(Receive receive);
}
