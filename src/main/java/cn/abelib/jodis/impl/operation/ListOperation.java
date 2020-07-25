package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisList;
import cn.abelib.jodis.impl.JodisObject;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:11
 */
public class ListOperation extends KeyOperation{

    public ListOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisList getJodisList(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisList)jodisObject.getValue();
    }

    private List<String> getList(String key) {
        if (exists(key)) {
            return getJodisList(key).getHolder();
        }
        return null;
    }

    /**
     *  todo
     * Redis command: BLPOP
     * @param key
     * @param timeout
     * @return
     */
    public String blockingLeftPop(String key, long timeout) {
        return null;
    }

    /**
     *  todo
     * Redis command: BLPOP
     * @param keys
     * @param timeout
     * @return
     */
    public String blockingLeftPop(Collection<String> keys, long timeout) {
        return null;
    }

    /**
     *  todo
     * Redis command: BLPOP
     * @param key
     * @param timeout
     * @return
     */
    public String blockingRightPop(String key, long timeout) {
        return null;
    }

    /**
     *  todo
     * Redis command: BRPOP
     * @param keys
     * @param timeout
     * @return
     */
    public String blockingRightPop(Collection<String> keys, long timeout) {
        return null;
    }

    /**
     *  todo
     * Redis command: RPOP
     * @return
     */
    public String rightPop(String key) {

        return null;
    }


    /**
     * todo
     * Redis command: BRPOPLPUSH
     * @param source
     * @param destination
     * @param timeout
     */
    public void blockingRightPopLeftPush(String source, String destination, long timeout) {

    }

    /**
     * todo
     * Redis command: LINDEX
     * @param key
     * @param index
     * @return
     */
    public String leftIndex(String key, int index) {
        if (!exists(key) || index >= listLength(key) || index < 0) {
            return null;
        }
        List<String> value = getList(key);
        return value.get(index);
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
        JodisList value =  getJodisList(key);
        return value.size();
    }

    /**
     * Redis command: LPUSH
     * @param key
     * @param values
     * @return
     */
    public int leftPush(String key, Collection<String> values) {
        List<String> value;
        if (exists(key)) {
            value =  getJodisList(key).getHolder();
        } else {
            value = Lists.newLinkedList();
        }
        value.addAll(0, values);
        return value.size();
    }

    /**
     * Redis command: LPUSH
     * @param key
     * @param value
     * @return
     */
    public int leftPush(String key, String value) {
        return leftPush(key, Collections.singletonList(value));
    }
}
