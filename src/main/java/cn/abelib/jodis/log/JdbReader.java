package cn.abelib.jodis.log;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.impl.SkipList;
import cn.abelib.jodis.utils.KV;
import com.google.common.collect.Sets;
import java.util.*;

import static cn.abelib.jodis.log.JdbConstant.*;
import static cn.abelib.jodis.utils.ByteUtils.*;

/**
 * @Author: abel.huang
 * @Date: 2020-07-20 01:29
 * Jdb means Jodis Database
 */
public class JdbReader {
    public JdbReader() {

    }

    public KV<String, JodisString> readString(byte[] bytes) {
        if (Objects.isNull(bytes) || bytes.length < 1) {
            return new KV<>();
        }
        int idx = JDB_TYPE_SIZE;
        int totalLen =  bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        int keyLen =  bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        String key = bytes2UTF8(slice(bytes, idx, keyLen));
        idx += keyLen;

        int valueLen =  bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        String value = bytes2UTF8(slice(bytes, idx, valueLen));
        return new KV<>(key, new JodisString(value));
    }

    public KV<String, JodisList> readList(byte[] bytes) {
        KV<String, List<String>> ans = readCollection(bytes);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
        JodisList jodisList = new JodisList(value);
        return new KV<>(key, jodisList);
    }

    public KV<String, JodisHash> readMap(byte[] bytes) {
        KV<String, List<String>> ans = readCollection(bytes);
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

    public KV<String, JodisSet> readSet(byte[] bytes) {
        KV<String, List<String>> ans = readCollection(bytes);
        if (ans.isNull()) {
            return new KV<>();
        }
        String key = ans.getK();
        List<String> value = ans.getV();
        JodisSet jodisSet = new JodisSet(Sets.newHashSet(value));
        return new KV<>(key, jodisSet);
    }

    public KV<String, JodisSortedSet> readZSet(byte[] bytes) {
        KV<String, List<String>> ans = readCollection(bytes);
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

    private KV<String, List<String>> readCollection(byte[] bytes) {
        if (Objects.isNull(bytes) || bytes.length < 1) {
            return new KV();
        }
        int idx = JDB_TYPE_SIZE;
        int totalLen =  bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        int keyLen =  bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        String key = bytes2UTF8(slice(bytes, idx, keyLen));
        idx += keyLen;

        // 集合长度
        int len = bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
        idx += JDB_INT_SIZE;

        List<String> value = new ArrayList<>(len);

        for (int i = 0; i < len ; i ++) {
            int itemLen = bytes2Int(slice(bytes, idx, JDB_INT_SIZE));
            idx += JDB_INT_SIZE;

            String item = bytes2UTF8(slice(bytes, idx, itemLen));
            idx += itemLen;
            value.add(item);
        }
        return new KV<>(key, value);
    }
}
