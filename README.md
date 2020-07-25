# Jodis(Java Object Dictionary Server)
Jodis is keyvalue db, the goal is to achieve most standalone feature for [Redis](https://github.com/redis-io/redis).

## Feature Support
### in-memory key-value database

### Support Structure
1. JodisString
2. JodisList
3. JodisMap
4. JodisSet
5. JodisZSet

### WAL
Similar with Redis AOF, but only support sync.
1. Sync per write operation;
2. Support rewrite log;
3. Request command queue;
4. Recover from AOF file.

### binary disk snapshot
Similar with Redis RDB.
1. Async per write operation base on aof;
2. Recover from snapshot.
### TTL(TODO)

### Network interface(TODO)
Multi Network Thread, Single I/O Thread.

### LRU

### CronJob

