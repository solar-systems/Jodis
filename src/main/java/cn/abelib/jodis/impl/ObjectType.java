package cn.abelib.jodis.impl;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:50
 */
public enum ObjectType {
    /**
     * JodisString
     */
    JODIS_STRING("string"),

    /**
     * JodisList
     */
    JODIS_LIST("list"),

    /**
     * JodisMap
     */
    JODIS_HASH("hash"),

    /**
     * JodisSet
     */
    JODIS_SET("set"),

    /**
     * JodisZSet
     */
    JODIS_ZSET("zset");

    ObjectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private String type;
}
