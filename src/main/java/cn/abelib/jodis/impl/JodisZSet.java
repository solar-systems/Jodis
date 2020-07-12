package cn.abelib.jodis.impl;


import cn.abelib.jodis.internals.SkipList;

import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class JodisZSet {
    private Map<String, Double> holder;
    private SkipList skipList;

    public JodisZSet(){}

    public JodisZSet(Map<String, Double> holder, SkipList skipList) {
        this.holder = holder;
        this.skipList = skipList;
    }

    public SkipList getSkipList() {
        return this.skipList;
    }

    public Map<String, Double> getHolder() {
        return this.holder;
    }

    public int size() {
        return this.holder.size();
    }
}
