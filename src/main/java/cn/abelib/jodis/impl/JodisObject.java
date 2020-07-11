package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.ExpireObject;

import java.time.Instant;

/**
 * @author abel.huang
 * @date 2020/6/30 17:42
 */
public class JodisObject<T> implements ExpireObject {
    private String type;

    private String encoding;

    private String lru;

    private int refCount;

    private long created;

    private long ttl;

    private T value;

    public JodisObject() {}

    public JodisObject(T value) {
        this(value, -1L);
    }

    public JodisObject(T value, long ttl) {
        this.value = value;
        this.created = Instant.now().getEpochSecond();
        this.ttl = ttl;
    }

    public T getValue() {
        return this.value;
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
