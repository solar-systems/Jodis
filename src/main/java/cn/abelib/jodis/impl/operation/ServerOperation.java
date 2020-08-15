package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ProtocolConstant;

/**
 * @Author: abel.huang
 * @Date: 2020-08-12 23:28
 */
public class ServerOperation extends AbstractOperation{
    public ServerOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    /**
     * Redis command: PING
     * @return
     */
    public String ping() {
        return ProtocolConstant.PONG;
    }

    /**
     * Redis command: DBSZIE
     * @return
     */
    public int dbSize() {
        return jodisDb.size();
    }

    /**
     * Redis command: FLUSHDB
     */
    public void flushDb() {
        jodisDb.clear();
    }
}
