package cn.abelib.jodis.impl.operation;


import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.impl.JodisSet;
import cn.abelib.jodis.utils.CollectionUtils;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 22:50
 */
public class SetOperation extends KeyOperation {

    public SetOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisSet getJodisSet(String key) {
        JodisObject jodisObject = jodisObject(key);
        if (Objects.isNull(jodisObject)) {
            return null;
        }
        return (JodisSet) jodisObject.getValue();
    }

    private Set<String> getSet(String key) {
        if (exists(key)) {
            return getJodisSet(key).getHolder();
        }
        return null;
    }

    private Set<String> getSets(String key) {
        if (exists(key)) {
            return getJodisSet(key).getHolder();
        }
        return Sets.newHashSet();
    }

    /**
     * Redis command: SADD
     *
     * @param key
     * @return
     */
    public int setAdd(String key, String member) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set)) {
            set = new HashSet<>();
            set.add(member);
            this.jodisDb.put(key, JodisObject.putJodisSet(set));
            return 0;
        }

        if (set.add(member)) {
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: SCARD
     *
     * @param key
     * @return
     */
    public int setCard(String key) {
        JodisSet set = getJodisSet(key);
        if (Objects.isNull(set)) {
            return 0;
        }

        return set.size();
    }

    /**
     * Redis command: SISMEMBER
     *
     * @param key
     * @param member
     * @return
     */
    public boolean setIsMember(String key, String member) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set)) {
            return false;
        }
        return set.contains(member);
    }

    /**
     * Redis command: SMEMBERS
     *
     * @param key
     * @return
     */
    public List<String> setMembers(String key) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(set);
    }

    /**
     * Redis command: SRANDMEMBER
     *
     * @param key
     * @return
     */
    public String setRandMember(String key) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set)) {
            return StringUtils.NIL;
        }
        return set.iterator().next();
    }

    /**
     * Redis command: SREM
     * @param key
     * @param member
     * @return
     */
    public boolean setRemove(String key, String member) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set)) {
            return false;
        }
        return set.remove(member);
    }

    /**
     * Redis command: SMOV
     *
     * @param source
     * @param destination
     * @param member
     */
    public boolean setMove(String source, String destination, String member) {
        if (!exists(source)) {
            return false;
        }
        setAdd(destination, member);
        return setRemove(source, member);
    }

    /**
     * Redis command: SDIFF
     * 只接受两个参数
     */
    public List<String> setDiff(String key1, String key2) {
        Set<String> set1 = getSets(key1);
        Set<String> set2 = getSets(key2);
        return Lists.newArrayList(CollectionUtils.diffSet(set1, set2));
    }

    /**
     * Redis command: SUNION
     * 只接受两个参数
     */
    public List<String> setUnion(String key1, String key2) {
        Set<String> set1 = getSets(key1);
        Set<String> set2 = getSets(key2);
        if (set1.isEmpty()) {
            return Lists.newArrayList(set2);
        }
        if (set2.isEmpty()) {
            return Lists.newArrayList(set1);
        }
        return Lists.newArrayList(CollectionUtils.unionSet(set1, set2));
    }

    /**
     * Redis command: SINTER
     * 只接受两个参数
     */
    public List<String> setInter(String key1, String key2) {
        Set<String> set1 = getSets(key1);
        Set<String> set2 = getSets(key2);
        if (set1.isEmpty() || set2.isEmpty()) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(CollectionUtils.interSet(set1, set2));
    }

    /**
     * todo
     * Redis command: SSCAN
     * @return
     */
    public List<String> setScan() {
        return Lists.newArrayList();
    }

    /**
     * Redis command: SPOP
     * @param key
     * @return
     */
    public String setPop(String key) {
        Set<String> set = getSet(key);
        if (Objects.isNull(set) || set.isEmpty()) {
            return StringUtils.NIL;
        }
        String member = set.iterator().next();
        set.remove(member);
        return member;
    }
}
