package cn.abelib.jodis.remoting;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.JodisHandler;
import cn.abelib.jodis.remoting.netty.NettySocketServer;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.Logger;

/**
 * 服务器工厂类，用于创建不同类型的服务器实例
 * 支持Java NIO Socket和Netty两种实现
 * 
 * @Author: abel.huang
 * @Date: 2025-03-21
 */
public class ServerFactory {
    private static final Logger logger = Logger.getLogger(ServerFactory.class);

    /**
     * 服务器类型枚举
     */
    public enum ServerType {
        /**
         * 基于Java NIO的实现
         */
        NIO("nio"),
        
        /**
         * 基于Netty的实现
         */
        NETTY("netty");

        private final String value;

        ServerType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ServerType fromString(String type) {
            if (type == null || type.isEmpty()) {
                return NIO; // 默认使用NIO
            }
            for (ServerType serverType : values()) {
                if (serverType.value.equalsIgnoreCase(type)) {
                    return serverType;
                }
            }
            logger.warn("Unknown server type: {}, using default NIO", type);
            return NIO;
        }
    }

    /**
     * 创建服务器实例
     * 
     * @param jodisDb 数据库实例
     * @param jodisConfig 配置
     * @return 服务器实例
     */
    public static Server createServer(JodisDb jodisDb, JodisConfig jodisConfig) {
        ServerType serverType = jodisConfig.getServerType();
        return createServer(jodisDb, jodisConfig, serverType);
    }

    /**
     * 创建指定类型的服务器实例
     * 
     * @param jodisDb 数据库实例
     * @param jodisConfig 配置
     * @param serverType 服务器类型
     * @return 服务器实例
     */
    public static Server createServer(JodisDb jodisDb, JodisConfig jodisConfig, ServerType serverType) {
        logger.info("Creating server of type: {}", serverType);
        
        switch (serverType) {
            case NETTY:
                return new NettySocketServer(jodisDb, jodisConfig);
            case NIO:
            default:
                JodisHandler jodisHandler = new JodisHandler(jodisDb);
                return new SocketServer(jodisHandler, jodisConfig);
        }
    }
}
