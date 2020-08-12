package cn.abelib.jodis.log;


import cn.abelib.jodis.utils.IoUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class AofWriter {
    private Path aofFile;
    private Path rewrite;

    public AofWriter(String dir, String fName) throws IOException {
        this.rewrite = Paths.get(dir, fName + ".rewrite");
        this.aofFile = IoUtils.createFileIfNotExists(dir, fName);
    }

    /**
     * 强制同步返回
     * @param reqCmd
     * @return
     */
    public boolean write(String reqCmd) throws IOException {
        Files.write(aofFile, reqCmd.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return true;
    }

    /**
     * 开启rewrite
     * @throws IOException
     */
    public void startRewrite() throws IOException {
        if (Files.exists(this.rewrite)) {
            Files.delete(rewrite);
        }
        Files.createFile(rewrite);
    }

    /**
     * AOF重写入磁盘文件
     * @param reqCmd
     * @return
     * @throws IOException
     */
    public boolean rewrite(String reqCmd) throws IOException {
        Files.write(rewrite, reqCmd.getBytes(StandardCharsets.UTF_8));
        return true;
    }
}
