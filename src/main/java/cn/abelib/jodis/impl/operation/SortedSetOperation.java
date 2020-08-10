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

    private SkipList getSkipList(String key) {
        JodisSortedSet set = getJodisZSet(key);
        if (Objects.isNull(set)) {
            return null;
        }
        return set.getSkipList();
    }

    private Map<String, Double> getHolder(String key) {
        JodisSortedSet set = getJodisZSet(key);
        if (Objects.isNull(set)) {
            return null;
        }
        return set.getHolder();
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
            this.jodisDb.put(key, JodisObject.putJodisZSet(zSet));
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

    /**
     * Redis command: ZCOUNT
     * @param key
     * @param min
     * @param max
     * @return
     */
    public int zCount(String key, double min, double max) {
        SkipList skipList = getSkipList(key);
        if (Objects.isNull(skipList)) {
            return 0;
        }
        int count = 0;

        for (double score : skipList.scores()) {
            if (score < min) {
                continue;
            }
            if (score >= min && score <= max) {
                count++;
            }
            if (score > max) {
                break;
            }
        }
        return count;
    }

    public int zRemove(String key, String member) {
        SkipList skipList = getSkipList(key);
        Map<String, Double> map = getHolder(key);
        if (Objects.isNull(skipList) || Objects.isNull(map)) {
            return 0;
        }
        Double score = map.get(member);
        if (Objects.isNull(score)) {
            return 0;
        }
        map.remove(member);
        skipList.delete(score, member);
        return 1;
    }
}
