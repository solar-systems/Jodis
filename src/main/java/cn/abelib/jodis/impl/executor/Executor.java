package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:31
 *  todo All Unit test case
 */
public interface Executor {

    /**
     * 具体执行
     * @param request
     * @return
     */
    Response execute(Request request);
}
