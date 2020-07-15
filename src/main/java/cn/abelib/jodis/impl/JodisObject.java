package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.ExpireObject;
import cn.abelib.jodis.internals.SkipList;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author abel.huang
 * @date 2020/6/30 17:42
 */
public class JodisObject implements ExpireObject {
    private String type;

    private String encoding;

    private String lru;

    private int rc;

    private long created;

    private long ttl;

    private Object value;

    public JodisObject() {}

    public JodisObject(Object value, String type, String encoding) {
        this(value, type, encoding, -1L);
    }

    public JodisObject(Object value, String type, String encoding, long ttl) {
        this.value = value;
        this.type = type;
        this.encoding = encoding;
        this.created = Instant.now().getEpochSecond();
        this.ttl = ttl;
    }

    public static JodisObject putJodisString(String value) {
        JodisString jodisString = new JodisString(value);
        return new JodisObject(jodisString,
                ObjectType.JODIS_STRING.getType(),
                EncodingType.OBJ_ENCODING_RAW.getType());
    }

    public static JodisObject putJodisList(List<String> value) {
        JodisList jodisList = new JodisList(value);
        return new JodisObject(jodisList,
                ObjectType.JODIS_LIST.getType(),
                EncodingType.OBJ_ENCODING_LIST.getType());
    }

    public static JodisObject putJodisSet(Set<String> value) {
        JodisSet jodisSet = new JodisSet(value);
        return new JodisObject(jodisSet,
                ObjectType.JODIS_SET.getType(),
                EncodingType.OBJ_ENCODING_SET.getType());
    }

    public static JodisObject putJodisMap(Map<String, String> value) {
        JodisMap jodisMap = new JodisMap(value);
        return new JodisObject(jodisMap,
                ObjectType.JODIS_HASH.getType(),
                EncodingType.OBJ_ENCODING_HT.getType());
    }

    public static JodisObject putJodisZSet(Map<String, Double> value, SkipList skipList) {
        JodisZSet jodisZSet = new JodisZSet(value, skipList);
        return new JodisObject(jodisZSet,
                ObjectType.JODIS_ZSET.getType(),
                EncodingType.OBJ_ENCODING_SKIPLIST.getType());
    }

    public Object getValue() {
        return this.value;
    }

    public String type() {
        return this.type;
    }

    public String encoding() {
        return this.encoding;
    }

    @Override
    public long created() {
        return this.created;
    }

    @Override
    public void created(long created) {
        this.created = created;
    }

    @Override
    public long ttl() {
        return this.ttl;
    }

    @Override
    public void ttl(long ttl) {
        this.ttl = ttl;
    }
}
