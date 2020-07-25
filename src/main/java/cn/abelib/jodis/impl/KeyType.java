package cn.abelib.jodis.impl;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:50
 */
public interface KeyType {
    /**
     * JodisString
     */
    String JODIS_STRING = "string";

    /**
     * JodisList
     */
    String JODIS_LIST = "list";

    /**
     * JodisHash
     */
    String JODIS_HASH = "hash";

    /**
     * JodisSet
     */
    String JODIS_SET = "set";

    /**
     * JodisSortedSet
     */
    String JODIS_ZSET = "zset";

    /**
     * None
     */
    String JODIS_NONE = "none";
}
