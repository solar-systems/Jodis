package cn.abelib.jodis.protocol;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 23:22
 * 解析参数异常
 */
public class RequestParserException extends RuntimeException{
    public RequestParserException(String message) {
        super(message);
    }
}
