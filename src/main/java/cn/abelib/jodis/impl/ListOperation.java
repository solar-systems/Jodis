package cn.abelib.jodis.impl;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:11
 */
public class ListOperation extends KeyOperation{

    public ListOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    /**
     * Redis command: LLEN
     * @param key
     * @return
     */
    public int listLength(String key) {
        if (!exists(key)) {
            return 0;
        }
        List<String> value =  (List<String>) jodisCollection.get(key).getValue();
        return value.size();
    }

    public int leftPush(String key, Collection<String> values) {
        List<String> value;
        if (exists(key)) {
            value =  (List<String>) jodisCollection.get(key).getValue();
        } else {
            value = Lists.newLinkedList();
        }
        value.addAll(0, values);
        jodisCollection.put(key, new JodisObject(value));
        return value.size();
    }

    public int leftPush(String key, String value) {
        return leftPush(key, Collections.singletonList(value));
    }
}
