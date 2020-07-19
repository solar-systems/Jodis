package cn.abelib.jodis.impl;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:50
 */
public interface ObjectType {
    /**
     * JodisString
     */
    String JODIS_STRING = "string";

    /**
     * JodisList
     */
    String JODIS_LIST = "list";

    /**
     * JodisMap
     */
    String JODIS_HASH = "hash";

    /**
     * JodisSet
     */
    String JODIS_SET = "set";

    /**
     * JodisZSet
     */
    String JODIS_ZSET ="zset";
}
