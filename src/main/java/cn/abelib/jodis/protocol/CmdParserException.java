package cn.abelib.jodis.protocol;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 23:22
 * 解析参数异常
 */
public class CmdParserException extends RuntimeException{
    public CmdParserException(String message) {
        super(message);
    }
}
