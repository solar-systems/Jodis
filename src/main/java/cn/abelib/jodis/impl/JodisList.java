package cn.abelib.jodis.impl;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:07
 */
public class JodisList {
    private List<String> holder;

    public JodisList() {
        holder = new LinkedList<>();
    }

    public JodisList(List<String> holder) {
        this.holder = holder;
    }

    public List<String> getHolder() {
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
