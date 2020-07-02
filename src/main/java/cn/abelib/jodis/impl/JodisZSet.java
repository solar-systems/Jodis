package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.JodisObject;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class JodisZSet<K> implements JodisObject {
    private ConcurrentSkipListSet<K> holder;
}
