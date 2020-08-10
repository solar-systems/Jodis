# Jodis(Java Object Dictionary Server)
Jodis is keyvalue db, the goal is to achieve most standalone feature for [Redis](https://github.com/redis-io/redis).

## Feature Support
### in-memory key-value database

### Support Structure
1. [JodisString](docs/JodisString.md)
2. [JodisList](docs/JodisList.md)
3. [JodisHash](docs/JodisHash.md)
4. [JodisSet](docs/JodisSet.md)
5. [JodisSortedSet](docs/JodisSortedSet.md)

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

### Network interface
1. Single Network Thread, Single I/O Thread;
2. Support RESP2 text protocol.

### LRU(TODO)

### TTL(TODO)


