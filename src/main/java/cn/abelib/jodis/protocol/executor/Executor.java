package cn.abelib.jodis.protocol.executor;

import cn.abelib.jodis.protocol.RespCmd;

import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:31
 */
public interface Executor {

    RespCmd execute(String cmd, List<String> params);

}
