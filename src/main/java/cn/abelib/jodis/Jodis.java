package cn.abelib.jodis;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Request;
import cn.abelib.jodis.protocol.Response;

import java.io.IOException;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:51
 * Java Object Dictionary Server
 */
public class Jodis {
    private JodisDb jodisDb;

    public Jodis() {}

    public Jodis(JodisConfig config) throws IOException {
        jodisDb = new JodisDb();
    }

    public static Jodis create() {
        return new Jodis();
    }

    public static Jodis create(JodisConfig config) throws IOException {
        return new Jodis(config);
    }

    public String process(String request) throws IOException {
        return jodisDb.execute(request).toRespString();
    }

    public Response process(Request request) throws IOException {
        return jodisDb.execute(request);
    }
}
