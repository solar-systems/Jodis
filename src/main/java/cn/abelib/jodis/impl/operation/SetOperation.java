package cn.abelib.jodis.impl.operation;


import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.impl.JodisSet;

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

    /**
     * Redis command: SADD
     *
     * @param key
     * @return
     */
    public int setAdd(String key, String member) {
        Set<String> set;
        if (exists(key)) {
            set = getSet(key);
        } else {
            set = new HashSet<>();
        }

        if (set.add(member)) {
            this.jodisDb.put(key, JodisObject.putJodisSet(set));
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
        if (!exists(key)) {
            return 0;
        }
        JodisSet set = getJodisSet(key);
        return Objects.isNull(set) ? 0 : set.size();
    }

    /**
     * Redis command: SISMEMBER
     *
     * @param key
     * @param member
     * @return
     */
    public boolean setIsMember(String key, String member) {
        if (!exists(key)) {
            return false;
        }
        Set<String> set = getSet(key);
        return set.contains(member);
    }

    /**
     * Redis command: SMEMBERS
     *
     * @param key
     * @return
     */
    public Set<String> setMembers(String key) {
        if (!exists(key)) {
            return null;
        }
        return getSet(key);
    }

    /**
     * Redis command: SRANDMEMBER
     *
     * @param key
     * @return
     */
    public String setRandMember(String key) {
        if (!exists(key)) {
            return null;
        }
        return getSet(key).iterator().next();
    }

    public Collection<String> setUnion(String... keys) {
        return null;
    }

    public Collection<String> setInner(String... keys) {
        return null;
    }

    public boolean setRemove(String key, String member) {
        if (!exists(key)) {
            return false;
        }
        Set<String> set = getSet(key);
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
     * todo
     * Redis command: SDIFFSTORE
     */
    public int setDiffStore() {
        return 0;
    }

    /**
     * todo
     * Redis command: SDIFF
     */
    public List<String> setDiff() {
        return null;
    }

    /**
     * todo
     * Redis command: SSCAN
     * @return
     */
    public List<String> setScan() {
        return null;
    }

    /**
     * todo
     * Redis command: SINTERSTORE
     */
    public int setInterStore() {
        return 0;
    }

    /**
     *  todo
     * Redis command: SUNIONSTORE
     */
    public int setUnionStore() {
        return 0;
    }

    /**
     * Redis command: SPOP
     * @param key
     * @return
     */
    public String setPop(String key) {
        if (!exists(key)) {
            return null;
        }
        Set<String> set = getSet(key);
        if (set.isEmpty()) {
            return null;
        }
        String member = set.iterator().next();
        set.remove(member);
        return member;
    }
}
