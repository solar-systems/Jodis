package cn.abelib.jodis;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.ReqCmd;
import cn.abelib.jodis.protocol.RespCmd;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:51
 * Java Object Dictionary Server
 */
public class Jodis {
    private JodisDb jodisDb;

    public Jodis() {}

    public Jodis(JodisConfig config) {
        jodisDb = new JodisDb();
    }

    public static Jodis create() {
        return new Jodis();
    }

    public static Jodis create(JodisConfig config) {
        return new Jodis(config);
    }

    public String process(String request) {
        return jodisDb.execute(request).toRespString();
    }

    public RespCmd process(ReqCmd request) {
        return jodisDb.execute(request);
    }
}
