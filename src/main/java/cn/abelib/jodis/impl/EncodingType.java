package cn.abelib.jodis.impl;

/**
 * @Author: abel.huang
 * @Date: 2020-07-12 17:21
 */
public enum EncodingType {
    /**
     * 原生String
     */
    OBJ_ENCODING_RAW("string"),

    /**
     * 整数
     */
    OBJ_ENCODING_INT("int"),

    /**
     * 哈希
     */
    OBJ_ENCODING_HT("hash"),

    /**
     * 集合
     */
    OBJ_ENCODING_SET("set"),

    /**
     * 链表
     */
    OBJ_ENCODING_LIST("list"),

    /**
     * 跳表
     */
    OBJ_ENCODING_SKIPLIST("skipList");

    EncodingType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private String type;
}
