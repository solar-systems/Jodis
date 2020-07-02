package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.JodisObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abel.huang
 * @date 2020/6/30 17:43
 */
public class JodisMap<K, V> implements JodisObject {
    private ConcurrentHashMap<K, V> holder;

}
