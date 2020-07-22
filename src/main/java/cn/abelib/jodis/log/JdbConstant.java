package cn.abelib.jodis.log;

/**
 * @Author: abel.huang
 * @Date: 2020-07-20 23:18
 */
public interface JdbConstant {
    String JDB_MAGIC_FLAG = "JODIS";
    String JDB_VERSION = "0001";

    byte EOF = 22;

    byte JDB_TYPE_STRING = 0;
    byte JDB_TYPE_LIST = 1;
    byte JDB_TYPE_HASH = 2;
    byte JDB_TYPE_SET = 3;
    byte JDB_TYPE_ZSET = 4;

    int JDB_TYPE_SIZE = 1;

    int JDB_INT_SIZE = 4;
}
