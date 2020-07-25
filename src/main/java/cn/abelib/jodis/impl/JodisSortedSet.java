package cn.abelib.jodis.impl;


import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class JodisSortedSet {
    private Map<String, Double> holder;
    private SkipList skipList;

    public JodisSortedSet(){}

    public JodisSortedSet(Map<String, Double> holder, SkipList skipList) {
        this.holder = holder;
        this.skipList = skipList;
    }

    public JodisSortedSet(Map<String, Double> holder) {
        this.holder = holder;
        this.skipList = new SkipList();
        holder.forEach((k, v) -> skipList.add(v, k));
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
