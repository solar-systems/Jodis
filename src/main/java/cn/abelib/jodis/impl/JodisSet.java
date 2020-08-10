package cn.abelib.jodis.impl;

import java.util.Set;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class JodisSet {
    private Set<String> holder;

    public JodisSet(){}

    public JodisSet(Set<String> holder) {
        this.holder = holder;
    }

    public Set<String> getHolder() {
        return this.holder;
    }

    public int size() {
        return this.holder.size();
    }

    @Override
    public String toString() {
        return this.holder.toString();
    }
}
