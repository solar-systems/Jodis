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
public class AofReader {
    private Path aofFile;
    private Path rewrite;

    public AofReader(String dir, String fName) throws IOException {
        this.rewrite = Paths.get(dir, fName + ".rewrite");
        this.aofFile = IoUtils.createFileIfNotExists(dir, fName);
    }

    public Iterator<String> readAof() throws IOException {
        return readFile(aofFile);
    }

    public Iterator<String> readRewrite() throws IOException {
        return readFile(rewrite);
    }

    public Iterator<String> readFile(Path path) throws IOException {
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
