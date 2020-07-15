package cn.abelib.jodis.persist;

import cn.abelib.jodis.protocol.ReqCmd;

import java.nio.file.Path;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class AofWriter {
    /**
     * 存放日志的跟路径
     */
    private Path dir;

    private String fName;

    /**
     * 强制同步返回
     * @param reqCmd
     * @return
     */
    public boolean write(ReqCmd reqCmd) {
        return false;
    }
}
