package cn.abelib.jodis.store;

import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class WalWriter {
    private Path aofFile;
    private Path rewrite;
    private RandomAccessFile raFile;
    private FileChannel channel;

    public WalWriter(String dir, String fName) throws IOException {
        this.rewrite = Paths.get(dir, fName + ".rewrite");
        this.aofFile = IoUtils.createFileIfNotExists(dir, fName);
        // 初始化 RandomAccessFile 用于 fsync
        this.raFile = new RandomAccessFile(this.aofFile.toFile(), "rw");
        this.channel = this.raFile.getChannel();
    }

    /**
     * 强制同步返回
     * @param reqCmd
     * @return
     */
    public boolean write(String reqCmd) throws IOException {
        reqCmd = reqCmd + StringUtils.CLRF;
        byte[] data = ByteUtils.getBytesUTF8(reqCmd);
        
        // 追加写入
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        
        // 强制刷盘，确保数据持久化到磁盘
        channel.force(true);
        
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
     * WAL 重写入磁盘文件（追加模式）
     * @param reqCmd
     * @return
     * @throws IOException
     */
    public boolean rewrite(String reqCmd) throws IOException {
        // 确保每个命令以 \r\n 结尾，方便后续按行读取
        if (!reqCmd.endsWith(StringUtils.CLRF)) {
            reqCmd = reqCmd + StringUtils.CLRF;
        }
        
        // 使用追加模式，避免覆盖文件
        Files.write(rewrite, 
                    reqCmd.getBytes(StandardCharsets.UTF_8), 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);
        return true;
    }
    
    /**
     * 关闭资源
     */
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (raFile != null) {
            raFile.close();
        }
    }
}
