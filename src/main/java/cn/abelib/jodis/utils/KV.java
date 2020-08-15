package cn.abelib.jodis.utils;

/**
 * @Author: abel.huang
 * @Date: 2020-07-21 22:59
 */
public class KV<K, V> {
    private K k;
    private V v;

    private boolean isNull;

    public KV() {
        this.isNull = true;
    }

    public KV(K k, V v) {
        this.k = k;
        this.v = v;
        this.isNull = false;
    }

    public K getK() {
        return k;
    }

    public V getV() {
        return v;
    }

    public void setK(K k) {
        this.k = k;
    }

    public void setV(V v) {
        this.v = v;
    }

    public boolean isNull() {
        return this.isNull;
    }

    @Override
    public String toString() {
        return "[K=" + k + ", V=" + v +"]";
    }
}
