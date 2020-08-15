package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.impl.SkipList;
import cn.abelib.jodis.utils.ByteUtils;
import cn.abelib.jodis.utils.KV;
import com.google.common.collect.Sets;

import java.nio.ByteBuffer;
import java.util.*;


/**
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 * Jdb means Jodis Database
 */
public class JdbReader {
    public JdbReader() {

    }

    public KV<String, JodisString> readString(ByteBuffer buffer) {
        if (Objects.isNull(buffer)) {
            return new KV<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KV<>();
        }
        int keyLen = buffer.getInt();
        String key = ByteUtils.toUTF8String(buffer, keyLen);
        int valueLen =  buffer.getInt();
        String value = ByteUtils.toUTF8String(buffer, valueLen);
        return new KV<>(key, new JodisString(value));
    }

    public KV<String, JodisList> readList(ByteBuffer buffer) {
        KV<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
        JodisList jodisList = new JodisList(value);
        return new KV<>(key, jodisList);
    }

    public KV<String, JodisHash> readMap(ByteBuffer buffer) {
        KV<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
        int len = value.size() / 2;
        Map<String, String> map = new HashMap<>(len);
        for (int i = 0; i < len; i++) {
            map.put(value.get(i * 2), value.get(i * 2 + 1));
        }
        JodisHash jodisHash = new JodisHash(map);
        return new KV<>(key, jodisHash);
    }

    public KV<String, JodisSet> readSet(ByteBuffer buffer) {
        KV<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
        JodisSet jodisSet = new JodisSet(Sets.newHashSet(value));
        return new KV<>(key, jodisSet);
    }

    public KV<String, JodisSortedSet> readZSet(ByteBuffer buffer) {
        KV<String, List<String>> ans = readCollection(buffer);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
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
        return new KV<>(key, jodisSortedSet);
    }

    private KV<String, List<String>> readCollection(ByteBuffer buffer) {
        if (Objects.isNull(buffer)) {
            return new KV<>();
        }
        buffer.flip();
        if (buffer.remaining() < 1) {
            return new KV<>();
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
        return new KV<>(key, value);
    }
}
