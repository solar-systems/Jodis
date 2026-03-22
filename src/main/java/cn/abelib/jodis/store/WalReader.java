package cn.abelib.jodis.store;

import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class WalReader {
    private Logger logger = Logger.getLogger(WalReader.class);
    private Path walFile;
    private Path rewrite;

    public WalReader(String dir, String fName) throws IOException {
        this.rewrite = Paths.get(dir, fName + ".rewrite");
        this.walFile = IoUtils.createFileIfNotExists(dir, fName);
    }

    public Iterator<String> readWal() throws IOException {
        logger.info("Read WAL file started");
        List<String> lines = new ArrayList<>();
        Iterator<String> iterator = readFile(walFile, "WAL");
        while (iterator.hasNext()) {
            lines.add(iterator.next());
        }
        logger.info("WAL file contains {} entries", lines.size());
        return lines.iterator();
    }

    public Iterator<String> readRewrite() throws IOException {
        return readFile(rewrite, "Rewrite WAL");
    }

    private Iterator<String> readFile(Path path, String fileType) throws IOException {
        if (!Files.exists(path)) {
            logger.info("{} file does not exist: {}", fileType, path.toAbsolutePath());
            return Collections.emptyIterator();
        }
        
        try {
            Iterable<String> iterable = IoUtils.readLines(path);
            // 将 Iterable 转换为 List
            List<String> lines = new ArrayList<>();
            for (String line : iterable) {
                if (line != null && !line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
            return lines.iterator();
        } catch (Exception e) {
            logger.error("Failed to read {} file: {}. Error: {}", fileType, path.toAbsolutePath(), e.getMessage());
            throw new IOException("Failed to read " + fileType + " file: " + path, e);
        }
    }
}
