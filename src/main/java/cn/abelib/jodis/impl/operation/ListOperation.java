package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisList;
import cn.abelib.jodis.impl.JodisObject;
import com.google.common.collect.Lists;

import java.util.*;

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
     * todo
     * Redis command: BRPOPLPUSH
     * @param source
     * @param destination
     * @param timeout
     */
    public void blockingRightPopLeftPush(String source, String destination, long timeout) {

    }

    /**
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
        List<String> value = getList(key);
        if (Objects.isNull(value)) {
            value = Lists.newLinkedList();
            jodisCollection.put(key, JodisObject.putJodisList(value));
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

    /**
     * Redis command: RPUSH
     * @param key
     * @param values
     * @return
     */
    public int rightPush(String key, Collection<String> values) {
        List<String> value = getList(key);
        if (Objects.isNull(value)) {
            value = Lists.newLinkedList();
            jodisCollection.put(key, JodisObject.putJodisList(value));
        }
        value.addAll(values);
        return value.size();
    }

    /**
     * Redis command: RPUSH
     * @param key
     * @param value
     * @return
     */
    public int rightPush(String key, String value) {
        return rightPush(key, Collections.singletonList(value));
    }

    /**
     * Redis command: LSET
     * @param key
     * @param index
     * @param value
     * @return
     */
    public int leftSet(String key, int index, String value) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return 0;
        }
        int len = values.size();
        if (index >= len || index < 0) {
            return 0;
        }  else {
            values.set(index, value);
        }
        return 1;
    }

    /**
     * Redis command: RPOP
     * @param key
     * @return
     */
    public String rightPop(String key) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return null;
        }
        int idx = values.size() - 1;
        String value = values.get(idx);
        values.remove(idx);
        return value;
    }

    /**
     * Redis command: LPOP
     * @param key
     * @return
     */
    public String leftPop(String key) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return null;
        }
        String value = values.get(0);
        values.remove(0);
        return value;
    }

    /**
     * Redis command: LINSERT
     * @param key
     * @param index
     * @param value
     * @return
     */
    public int leftInsert(String key, int index, String value) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return 0;
        }
        int len = values.size();
        if (index > len || index < 0) {
            return 0;
        } else if (index == len) {
            values.add(value);
        } else {
            values.add(index, value);
        }
        return 1;
    }

    /**
     * Redis command: LTRIM
     * @param key
     * @param start
     * @param end
     * @return
     */
    public boolean leftTrim(String key, int start, int end) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return false;
        }
        List<String> subString = listRange(key, start, end);
        values.clear();
        if (Objects.isNull(subString) || subString.isEmpty()) {
            delete(key);
        }
        values.addAll(subString);
        return true;
    }

    /**
     * Redis command: LLEN
     * @param key
     * @return
     */
    public int leftLength(String key) {
        JodisList values = getJodisList(key);
        if (Objects.isNull(values)) {
            return 0;
        }
        return values.size();
    }

    /**
     * Redis command: LREM
     * @param key
     * @param count
     * @param value
     * @return
     */
    public int listRemove(String key, int count, String value) {
        List<String> values = getList(key);
        if (Objects.isNull(values)) {
            return 0;
        }
        int len = values.size();
        if (len == 1) {

        }
        int cnt = count;
        Iterator<String> iterator = values.iterator();
        if (count == 0) {
            while (iterator.hasNext()) {
                String val = iterator.next();
                if (value.equals(val)) {
                    iterator.remove();
                    cnt --;
                }
            }
        } else if (count > 0) {
            while (iterator.hasNext()) {
                String val = iterator.next();
                if (value.equals(val)) {
                    iterator.remove();
                    cnt --;
                    if (cnt == 0) {
                        break;
                    }
                }
            }
        } else {
            for (int i = len - 1; i >= 0; i--) {
                if (value.equals(values.get(i))) {
                    values.remove(value);
                    cnt ++;
                    if (cnt == 0) {
                        break;
                    }
                }
            }
        }
        return Math.abs(cnt - count);
    }

    /**
     * Redis command: LRANGE
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> listRange(String key, int start, int end) {
        List<String> value = getList(key);
        if (Objects.isNull(value)) {
           return value;
        }
        int len = value.size();
        if (start < 0 || end >= len || start > end) {
            return null;
        }
        return value.subList(start, end);
    }
}
