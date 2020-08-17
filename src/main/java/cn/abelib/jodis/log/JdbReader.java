package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.impl.SkipList;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.IoUtils;
import cn.abelib.jodis.utils.KeyValue;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;


/**
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 * Jdb means Jodis Database
 */
public class JdbReader {
    private Path jdbFile;

    public JdbReader() {

    }

    public JdbReader(String dir, String fName) throws IOException {
        this.jdbFile = IoUtils.createFileIfNotExists(dir, fName);
    }

    public KeyValue<String, JodisString> readString(ByteBuffer buffer) {
        if (Objects.isNull(buffer)) {
            return new KeyValue<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KeyValue<>();
        }
        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        int valueLen =  buffer.getInt();
        String value = ByteUtils.toUTF8String(buffer, valueLen);
        return new KeyValue<>(key, new JodisString(value));
    }

    public KeyValue<String, JodisList> readList(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        JodisList jodisList = new JodisList(value);
        return new KeyValue<>(key, jodisList);
    }

    public KeyValue<String, JodisHash> readMap(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        int len = value.size() / 2;
        Map<String, String> map = new HashMap<>(len);
        for (int i = 0; i < len; i++) {
            map.put(value.get(i * 2), value.get(i * 2 + 1));
        }
        JodisHash jodisHash = new JodisHash(map);
        return new KeyValue<>(key, jodisHash);
    }

    public KeyValue<String, JodisSet> readSet(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        JodisSet jodisSet = new JodisSet(Sets.newHashSet(value));
        return new KeyValue<>(key, jodisSet);
    }

    public KeyValue<String, JodisSortedSet> readZSet(ByteBuffer buffer) {
        KeyValue<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KeyValue<>();
        }
        String key = ans.getKey();
        List<String> value = ans.getValue();
        int len = value.size() / 2;
        Map<String, Double> map = new HashMap<>(len);
        SkipList skipList = new SkipList();
        for (int i = 0; i < len; i++) {
            String item = value.get(i * 2);
            Double score = Double.valueOf(value.get(i * 2 + 1));
            map.put(item, score);
            skipList.add(score, item);
        }
        JodisSortedSet jodisSortedSet = new JodisSortedSet(map, skipList);
        return new KeyValue<>(key, jodisSortedSet);
    }

    private KeyValue<String, List<String>> readCollection(ByteBuffer buffer) {
        if (Objects.isNull(buffer)) {
            return new KeyValue<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KeyValue<>();
        }
        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        // 集合长度
        int len = buffer.getInt();
        List<String> value = new ArrayList<>(len);
        for (int i = 0; i < len ; i ++) {
            int itemLen = buffer.getInt();
            String item = ByteUtils.toUTF8String(buffer, itemLen);
            value.add(item);
        }
        return new KeyValue<>(key, value);
    }
}
