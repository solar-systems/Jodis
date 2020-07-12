package cn.abelib.jodis.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/6/30 17:43
 */
public class JodisMap {
    private Map<String, String> holder;

    public JodisMap(){
        holder = new HashMap<>(8);
    }

    public JodisMap(Map<String, String> holder) {
        this.holder = holder;
    }

    public Map<String, String> getHolder() {
        return this.holder;
    }

    public int size() {
        return this.holder.size();
    }
}