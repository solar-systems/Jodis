package cn.abelib.jodis.impl;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
        return (JodisSet)jodisObject.getValue();
    }

    /**
     * Redis command: SADD
     * @param key
     * @return
     */
    public int sadd(String key, String member){
        Set<String> set;
        if (exists(key)) {
            set = getJodisSet(key).getHolder();
        } else {
            set = new HashSet<>();
        }

        if (set.add(member)) {
            jodisCollection.put(key, JodisObject.putJodisSet(set));
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: SCARD
     * @param key
     * @return
     */
    public int scard(String key) {
        if (!exists(key)) {
            return 0;
        }
        JodisSet set = getJodisSet(key);
        return Objects.isNull(set) ? 0 : set.size();
    }

    /**
     * Redis command: SISMEMBER
     * @param key
     * @param member
     * @return
     */
    public boolean setIsMember(String key, String member) {
        if (!exists(key)) {
            return false;
        }
        Set<String> set = getJodisSet(key).getHolder();
        return set.contains(member);
    }

    /**
     * Redis command: SMEMBERS
     * @param key
     * @return
     */
    public Set<String> setMembers(String key) {
        if (!exists(key)) {
            return null;
        }
       return getJodisSet(key).getHolder();
    }

    /**
     * Redis command: SRANDMEMBER
     * @param key
     * @return
     */
    public String setRandMember(String key) {
        if (!exists(key)) {
            return null;
        }
        return getJodisSet(key).getHolder().iterator().next();
    }
}
