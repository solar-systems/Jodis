package cn.abelib.jodis.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @Author: abel.huang
 * @Date: 2020-07-03 01:01
 */
public class IoUtils {
    private IoUtils(){}

    public static Path createFileIfNotExists(String parent, String fileName) throws IOException {
        if (StringUtils.isEmpty(parent)) {
            return null;
        }
        Path path = Paths.get(parent);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        Path file = path.resolve(fileName);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        return file;
    }

    /**
     * 返回文件单行内容的迭代器
     * @param path
     * @return
     * @throws IOException
     */
    public static Iterable<String> readLines(Path path) throws IOException {
        final BufferedReader br = Files.newBufferedReader(path);
        return () -> new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return line != null;
            }

            @Override
            public String next() {
                String thisLine = line;
                line = nextLine();
                return thisLine;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            String nextLine() {
                String line;
                try {
                    line = br.readLine();
                } catch (IOException ioEx) {
                    line = null;
                }
                return line;
            }

            String line = nextLine();
        };
    }

    public static Iterable<String> readLines(String filename) throws IOException {
        return readLines(Paths.get(filename));
    }
}

