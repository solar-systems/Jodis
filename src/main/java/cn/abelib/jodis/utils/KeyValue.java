package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-07-21 22:59
 */
public class KeyValue<K, V> {
    private K key;
    private V value;

    private boolean isNull;
    
    private long expireTime;  // TTL 过期时间戳

    public KeyValue() {
        this.isNull = true;
        this.expireTime = -1;
    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
        this.isNull = false;
        this.expireTime = -1;
    }
    
    public KeyValue(K key, V value, long expireTime) {
        this.key = key;
        this.value = value;
        this.isNull = false;
        this.expireTime = expireTime;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isNull() {
        return this.isNull;
    }
    
    public long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "[Key=" + key + ", Value=" + value +"]";
    }
}
