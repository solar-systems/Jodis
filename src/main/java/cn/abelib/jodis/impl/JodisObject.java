package cn.abelib.jodis.impl;

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
        this.created = Instant.now().toEpochMilli(); // 使用毫秒时间戳
        this.ttl = ttl; // ttl 以毫秒为单位
    }

    public static JodisObject putJodisString(String value) {
        JodisString jodisString = new JodisString(value);
        return new JodisObject(jodisString,
                KeyType.JODIS_STRING,
                EncodingType.OBJ_ENCODING_RAW.getType());
    }

    public static JodisObject putJodisList(List<String> value) {
        JodisList jodisList = new JodisList(value);
        return new JodisObject(jodisList,
                KeyType.JODIS_LIST,
                EncodingType.OBJ_ENCODING_LIST.getType());
    }

    public static JodisObject putJodisSet(Set<String> value) {
        JodisSet jodisSet = new JodisSet(value);
        return new JodisObject(jodisSet,
                KeyType.JODIS_SET,
                EncodingType.OBJ_ENCODING_SET.getType());
    }

    public static JodisObject putJodisHash(Map<String, String> value) {
        JodisHash jodisHash = new JodisHash(value);
        return new JodisObject(jodisHash,
                KeyType.JODIS_HASH,
                EncodingType.OBJ_ENCODING_HT.getType());
    }

    public static JodisObject putJodisZSet(Map<String, Double> value, SkipList skipList) {
        JodisSortedSet jodisSortedSet = new JodisSortedSet(value, skipList);
        return new JodisObject(jodisSortedSet,
                KeyType.JODIS_ZSET,
                EncodingType.OBJ_ENCODING_SKIPLIST.getType());
    }

    public static JodisObject putJodisZSet(JodisSortedSet zSet) {
        JodisSortedSet jodisSortedSet = new JodisSortedSet(zSet);
        return new JodisObject(jodisSortedSet,
                KeyType.JODIS_ZSET,
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
    
    /**
     * 获取过期时间戳（毫秒）
     * @return 过期时间戳，如果没有设置 TTL 则返回 -1
     */
    public long getExpireTime() {
        if (this.ttl <= 0) {
            return -1;  // 永不过期
        }
        return this.created + this.ttl;
    }
    
    /**
     * 设置过期时间戳
     * @param expireTime 过期时间戳（毫秒）
     */
    public void setExpireTime(long expireTime) {
        if (expireTime <= 0) {
            this.ttl = -1;
        } else {
            this.ttl = expireTime - this.created;
        }
    }
}
