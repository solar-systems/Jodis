package cn.abelib.jodis.store;

import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * WAL (Write-Ahead Log) 读取器
 *
 * WAL 不适合使用 mmap，使用 BufferedReader 按行读取
 *
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class WalReader {
    private static final Logger logger = Logger.getLogger(WalReader.class);

    private Path walFilePath;
    private Path rewriteFilePath;

    /**
     * 使用 StoreConfig 创建 WAL 读取器
     *
     * @param config 存储配置
     * @throws IOException IO异常
     */
    public WalReader(StoreConfig config) throws IOException {
        this(config.getLogDir(), config.getWalFile());
    }

    /**
     * 创建 WAL 读取器
     *
     * @param dir   目录
     * @param fName 文件名
     * @throws IOException IO异常
     */
    public WalReader(String dir, String fName) throws IOException {
        String basePath = dir + (dir.endsWith("/") ? "" : "/");
        this.rewriteFilePath = Paths.get(basePath + fName + ".rewrite");
        this.walFilePath = IoUtils.createFileIfNotExists(dir, fName);
    }

    /**
     * 读取 WAL 文件
     *
     * @return 命令迭代器
     * @throws IOException IO异常
     */
    public Iterator<String> readWal() throws IOException {
        logger.info("Reading WAL file: {}", walFilePath);
        List<String> lines = new ArrayList<>();
        Iterator<String> iterator = readFile(walFilePath, "WAL");
        while (iterator.hasNext()) {
            lines.add(iterator.next());
        }
        logger.info("WAL file contains {} entries", lines.size());
        return lines.iterator();
    }

    /**
     * 读取 Rewrite 文件
     *
     * @return 命令迭代器
     * @throws IOException IO异常
     */
    public Iterator<String> readRewrite() throws IOException {
        return readFile(rewriteFilePath, "Rewrite WAL");
    }

    /**
     * 读取文件内容
     */
    private Iterator<String> readFile(Path path, String fileType) throws IOException {
        if (!Files.exists(path)) {
            logger.info("{} file does not exist: {}", fileType, path.toAbsolutePath());
            return Collections.emptyIterator();
        }

        try {
            // 使用 BufferedReader 按行读取，内存效率高
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line);
                    }
                }
            }
            logger.info("Read {} file completed, {} entries", fileType, lines.size());
            return lines.iterator();
        } catch (Exception e) {
            logger.error("Failed to read {} file: {}. Error: {}", fileType, path.toAbsolutePath(), e.getMessage());
            throw new IOException("Failed to read " + fileType + " file: " + path, e);
        }
    }

    /**
     * 检查 WAL 文件是否存在
     */
    public boolean exists() {
        return Files.exists(walFilePath) && walFilePath.toFile().length() > 0;
    }

    /**
     * 检查 Rewrite 文件是否存在
     */
    public boolean rewriteExists() {
        return Files.exists(rewriteFilePath) && rewriteFilePath.toFile().length() > 0;
    }

    /**
     * 获取 WAL 文件大小
     */
    public long getWalFileSize() throws IOException {
        return Files.exists(walFilePath) ? Files.size(walFilePath) : 0;
    }

    /**
     * 获取 WAL 文件路径
     */
    public Path getWalFilePath() {
        return walFilePath;
    }

    /**
     * 获取 Rewrite 文件路径
     */
    public Path getRewriteFilePath() {
        return rewriteFilePath;
    }
}
