package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.protocol.Response;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:31
 */
public interface Executor {

    Response execute(String cmd, List<String> params);

}
