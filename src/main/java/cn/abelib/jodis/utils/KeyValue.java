package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-07-21 22:59
 */
public class KeyValue<K, V> {
    private K key;
    private V value;

    private boolean isNull;

    public KeyValue() {
        this.isNull = true;
    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
        this.isNull = false;
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

    @Override
    public String toString() {
        return "[Key=" + key + ", Value=" + value +"]";
    }
}
