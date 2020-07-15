package cn.abelib.jodis.impl;

import cn.abelib.jodis.protocol.CmdConstant;
import cn.abelib.jodis.protocol.ErrorCmd;
import cn.abelib.jodis.protocol.ReqCmd;
import cn.abelib.jodis.protocol.RespCmd;
import cn.abelib.jodis.protocol.executor.ExecutorFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 */
public class JodisDb {
    /**
     * 存储不带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> jodisCollection;
    /**
     * 存储带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    private ExecutorFactory executorFactory;

    public JodisDb() {
        jodisCollection = new ConcurrentHashMap<>();
        executorFactory = new ExecutorFactory(this);
    }

    public ConcurrentHashMap<String, JodisObject> jodisCollection() {
        return this.jodisCollection;
    }

    public ConcurrentHashMap<String, JodisObject> expireJodisCollection() {
        return this.expireJodisCollection;
    }

    public JodisObject put(String key, JodisObject value) {
        return jodisCollection.put(key, value);
    }

    /**
     * 执行命令
     * @param request
     * @return
     */
    public RespCmd execute(ReqCmd request) {
        String cmd = request.getCmd();
        List<String> params = request.getParams();

        if (CmdConstant.KEY_CMDS.contains(cmd)) {
            // todo
            return executorFactory.stringExecutor().execute(cmd, params);
        } else if (CmdConstant.STRING_CMDS.contains(cmd)) {
            return executorFactory.stringExecutor().execute(cmd, params);
        } else if (CmdConstant.LIST_CMDS.contains(cmd)) {
            return executorFactory.listExecutor().execute(cmd, params);
        } else if (CmdConstant.HASH_CMDS.contains(cmd)) {
            return executorFactory.hashExecutor().execute(cmd, params);
        } else if (CmdConstant.SET_CMDS.contains(cmd)) {
            return executorFactory.setExecutor().execute(cmd, params);
        } else if (CmdConstant.ZSET_CMDS.contains(cmd)) {
            return executorFactory.zSetExecutor().execute(cmd, params);
        } else {
            // todo not support now
        }
        // todo
        return new ErrorCmd();
    }



    public RespCmd execute(String request) {
        ReqCmd reqCmd = new ReqCmd(request);
        return execute(reqCmd);
    }
}
