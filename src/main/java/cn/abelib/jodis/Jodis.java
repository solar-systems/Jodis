package cn.abelib.jodis;

import cn.abelib.jodis.api.JodisObject;
import cn.abelib.jodis.protocol.RequestCommand;
import cn.abelib.jodis.protocol.RespCommand;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abel.huang
 * @date 2020/6/30 17:40
 * Java Object Dictionary Server
 */
public class Jodis {
    /**
     * 存储不带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> jodisCollection;
    /**
     * 存储带过期时间的key
     */
    private ConcurrentHashMap<String, JodisObject> expireJodisCollection;

    public Jodis() {
        jodisCollection = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, JodisObject> jodisCollection() {
        return this.jodisCollection;
    }

    public ConcurrentHashMap<String, JodisObject> expireJodisCollection() {
        return this.expireJodisCollection;
    }

    /**
     * 执行命令
     * @param request
     * @return
     */
    public RespCommand execute(RequestCommand request) {
        return null;
    }
}
