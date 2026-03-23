package cn.abelib.jodis.store;

import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.Logger;
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
 * WAL (Write-Ahead Log) 写入器
 *
 * WAL 不适合使用 mmap，原因：
 * 1. 追加写入模式：每次操作都追加写入
 * 2. 需要立即刷盘：每次写入后要 force() 保证持久性
 * 3. 文件持续增长：需要频繁重新映射
 * 4. 小数据量高频写入：mmap 映射开销大于收益
 *
 * 使用 FileChannel + force 实现，保证数据持久性
 *
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class WalWriter {
    private static final Logger logger = Logger.getLogger(WalWriter.class);

    private static final int BUFFER_SIZE = 8 * 1024; // 8KB 缓冲区

    private Path walFilePath;
    private Path rewriteFilePath;
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private volatile boolean closed = false;

    /**
     * 使用 StoreConfig 创建 WAL 写入器
     *
     * @param config 存储配置
     * @throws IOException IO异常
     */
    public WalWriter(StoreConfig config) throws IOException {
        this(config.getLogDir(), config.getWalFile());
    }

    /**
     * 创建 WAL 写入器
     *
     * @param dir   目录
     * @param fName 文件名
     * @throws IOException IO异常
     */
    public WalWriter(String dir, String fName) throws IOException {
        String basePath = dir + (dir.endsWith("/") ? "" : "/");
        this.rewriteFilePath = Paths.get(basePath + fName + ".rewrite");
        this.walFilePath = IoUtils.createFileIfNotExists(dir, fName);
        // 初始化 RandomAccessFile 用于追加写入和 fsync
        this.randomAccessFile = new RandomAccessFile(this.walFilePath.toFile(), "rw");
        this.fileChannel = this.randomAccessFile.getChannel();
        // 移动到文件末尾，准备追加写入
        this.fileChannel.position(this.fileChannel.size());

        logger.info("WalWriter initialized, file: {}, position: {}", walFilePath, fileChannel.position());
    }

    /**
     * 写入命令到 WAL 文件
     * 使用追加模式写入，并强制刷盘确保数据持久化
     *
     * @param reqCmd Redis 命令
     * @return 写入成功返回 true
     * @throws IOException IO异常
     */
    public boolean write(String reqCmd) throws IOException {
        if (closed) {
            logger.warn("WalWriter is closed, cannot write");
            return false;
        }

        if (reqCmd == null || reqCmd.isEmpty()) {
            return false;
        }

        // 确保每个命令以 \r\n 结尾
        reqCmd = reqCmd + StringUtils.CLRF;
        byte[] data = ByteUtils.getBytesUTF8(reqCmd);

        // 使用 ByteBuffer 写入
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            fileChannel.write(buffer);
        }

        // 强制刷盘，确保数据持久化到磁盘
        // WAL 必须每次写入都刷盘，保证数据不丢失
        fileChannel.force(false);

        return true;
    }

    /**
     * 批量写入命令到 WAL 文件
     * 适用于批量操作场景，减少刷盘次数
     *
     * @param reqCmds Redis 命令列表
     * @return 写入成功返回 true
     * @throws IOException IO异常
     */
    public boolean writeBatch(Iterable<String> reqCmds) throws IOException {
        if (closed) {
            logger.warn("WalWriter is closed, cannot write");
            return false;
        }

        if (reqCmds == null) {
            return false;
        }

        // 计算总大小
        int totalSize = 0;
        for (String cmd : reqCmds) {
            if (cmd != null) {
                totalSize += cmd.getBytes(StandardCharsets.UTF_8).length + 2; // +2 for \r\n
            }
        }

        // 使用单个 ByteBuffer 写入所有命令
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        for (String cmd : reqCmds) {
            if (cmd != null) {
                buffer.put(ByteUtils.getBytesUTF8(cmd));
                buffer.put(StringUtils.CLRF.getBytes(StandardCharsets.UTF_8));
            }
        }
        buffer.flip();

        // 写入
        while (buffer.hasRemaining()) {
            fileChannel.write(buffer);
        }

        // 批量写入只需刷盘一次
        fileChannel.force(false);

        return true;
    }

    /**
     * 开启 rewrite
     *
     * @throws IOException IO异常
     */
    public void startRewrite() throws IOException {
        if (Files.exists(this.rewriteFilePath)) {
            Files.delete(rewriteFilePath);
        }
        Files.createFile(rewriteFilePath);
        logger.info("Rewrite file created: {}", rewriteFilePath);
    }

    /**
     * WAL 重写入磁盘文件（追加模式）
     *
     * @param reqCmd Redis 命令
     * @return 写入成功返回 true
     * @throws IOException IO异常
     */
    public boolean rewrite(String reqCmd) throws IOException {
        // 确保每个命令以 \r\n 结尾，方便后续按行读取
        if (!reqCmd.endsWith(StringUtils.CLRF)) {
            reqCmd = reqCmd + StringUtils.CLRF;
        }

        // 使用追加模式，避免覆盖文件
        Files.write(rewriteFilePath,
                reqCmd.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
        return true;
    }

    /**
     * 完成 rewrite，用 rewrite 文件替换原 WAL 文件
     *
     * @throws IOException IO异常
     */
    public void finishRewrite() throws IOException {
        // 关闭当前文件
        close();

        // 删除原 WAL 文件
        if (Files.exists(walFilePath)) {
            Files.delete(walFilePath);
        }

        // 重命名 rewrite 文件为 WAL 文件
        if (Files.exists(rewriteFilePath)) {
            Files.move(rewriteFilePath, walFilePath);
            logger.info("Rewrite completed, new WAL file: {}", walFilePath);
        }

        // 重新打开 WAL 文件
        this.randomAccessFile = new RandomAccessFile(this.walFilePath.toFile(), "rw");
        this.fileChannel = this.randomAccessFile.getChannel();
        this.fileChannel.position(this.fileChannel.size());
        this.closed = false;
    }

    /**
     * 获取当前写入位置
     */
    public long getPosition() throws IOException {
        return fileChannel != null ? fileChannel.position() : 0;
    }

    /**
     * 获取 WAL 文件大小
     */
    public long getFileSize() throws IOException {
        return walFilePath != null && Files.exists(walFilePath) ? Files.size(walFilePath) : 0;
    }

    /**
     * 获取 WAL 文件路径
     */
    public Path getWalFilePath() {
        return walFilePath;
    }

    /**
     * 关闭资源
     */
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (fileChannel != null && fileChannel.isOpen()) {
                // 最后一次刷盘
                fileChannel.force(false);
                fileChannel.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            logger.info("WalWriter closed");
        }
    }
}
