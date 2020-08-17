package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:31
 */
public interface Executor {

    /**
     * 具体执行
     * @param request
     * @return
     */
    Response execute(Request request);
}
