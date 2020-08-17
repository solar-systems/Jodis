# Jodis(Java Object Dictionary Server)
Jodis是一个基于内存的NoSQL键值数据库，支持独立部署和嵌入式使用。

## Overview
### 内存k-v数据库
数据读写基于内存。

### 支持多种数据结构
1. [JodisString](docs/JodisString.md)
用于存储普通的字符串，底层基于java.lang.String。
2. [JodisList](docs/JodisList.md)
列表对象，底层基于java.util.List;
3. [JodisHash](docs/JodisHash.md)
哈希表结构，底层基于java.util.Map;
4. [JodisSet](docs/JodisSet.md)
集合结构，底层基于java.util.Set;
5. [JodisSortedSet](docs/JodisSortedSet.md)
有序集合结构，底层基于java.util.Map和跳跃表。

### 单线程Reactor服务器
同时支持多个连接，命令执行为单线程。

### 兼容Redis RESP2协议
兼容大部分Redis命令，具体支持的命令如下：

SERVER: [PING, FLUSHDB, DBSIZE](docs/JodisString.md)

KEY: [DEL, TYPE, EXISTS, KEYS, RANDOMKEY, RENAME, RENAMENX, EXPIRE, EXPIREAT, TTL](docs/JodisKey.md)

JODIS_STRING: [GET, SET, GETSET, GETRANGE, MGET, MSET, SETEX, SETNX, SETRANGE, STRLEN, INCR, INCRBY, INCRBYFLOAT, DECR, DECRBY, APPEND](docs/JodisString.md)

JODIS_LIST: [LINDEX, LINSERT, LPOP, LPUSH, LRANGE, LSET, RPOP,  RPUSH](docs/JodisList.md)

JODIS_HASH: [HDEL, HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HVALS,, HLEN, HMGET, HMSET, HSETNX, HSCAN](docs/JodisHash.md)

JODIS_ZSE： [ZADD, ZCARD, ZCOUNT, ZSCORE, ZREM](docs/JodisSortedSet.md)

### 磁盘持久化
1. WAL日志
类似于Redis的AOF.
2. JDB二进制Dump
类似于Redis的RDB。


## TODO List
1. LRU；
2. TTL；
3. IO优化；
4. WAL rewrite后台任务和JDB后台任务；
5. 代码完善优化，持续重构，完善单元测试，JMH测试；
6. 文档和Example编写;
7. Docker支持。



