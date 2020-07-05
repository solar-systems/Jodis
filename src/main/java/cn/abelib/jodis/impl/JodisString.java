package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.JodisObject;

/**
 * @author abel.huang
 * @date 2020/6/30 17:42
 */
public class JodisString implements JodisObject {
    private String holder;
    private int len;
    public JodisString(String value) {
        this.holder = value;
        this.len = value.length();
    }

    public String getHolder() {
        return this.holder;
    }

    public int getLen() {
        return this.len;
    }
}
