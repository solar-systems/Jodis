package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.impl.JodisSortedSet;
import cn.abelib.jodis.impl.SkipList;

import java.util.Map;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-07-12 23:16
 */
public class SortedSetOperation extends KeyOperation {
    public SortedSetOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisSortedSet getJodisZSet(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisSortedSet)jodisObject.getValue();
    }

    /**
     * Redis command: ZADD
     * @param key
     * @return
     */
    public int zAdd(String key, double score, String member){
        JodisSortedSet zSet;
        if (exists(key)) {
            zSet = getJodisZSet(key);
        } else {
            zSet = new JodisSortedSet();
            jodisCollection.put(key, JodisObject.putJodisZSet(zSet));
        }
        Map<String, Double> map = zSet.getHolder();
        SkipList skipList = zSet.getSkipList();

        if (Objects.isNull(map.put(member, score))) {
            skipList.add(score, member);
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: ZCARD
     * @param key
     * @return
     */
    public int zCard(String key) {
        if (!exists(key)) {
            return 0;
        }
        JodisSortedSet set = getJodisZSet(key);
        return Objects.isNull(set) ? 0 : set.size();
    }

    /**
     * Redis command: ZSCORE
     * @param key
     * @param member
     * @return
     */
    public double zScore(String key, String member) {
        if (!exists(key)) {
            return 0.0;
        }
        JodisSortedSet set = getJodisZSet(key);
        Map<String, Double> map = set.getHolder();
        return map.get(member);
    }
}
