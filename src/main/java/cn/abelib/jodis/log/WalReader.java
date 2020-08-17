package cn.abelib.jodis.log;

import cn.abelib.jodis.utils.IoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @Author: abel.huang
 * @Date: 2020-07-14 22:51
 */
public class WalReader {
    private Path walFile;
    private Path rewrite;

    public WalReader(String dir, String fName) throws IOException {
        this.rewrite = Paths.get(dir, fName + ".rewrite");
        this.walFile = IoUtils.createFileIfNotExists(dir, fName);
    }

    public Iterator<String> readWal() throws IOException {
        return readFile(walFile);
    }

    public Iterator<String> readRewrite() throws IOException {
        return readFile(rewrite);
    }

    private Iterator<String> readFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public String next() {
                    return null;
                }
            };
        }
        return IoUtils.readLines(path).iterator();
    }
}
