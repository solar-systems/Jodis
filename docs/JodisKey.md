## JodisKey

### DEL
如果 key 存在，删除 key 并返回 1，否则返回 0

### TYPE
返回 key 所存储的值的类型

### EXISTS
检查 key 是否存在，存在返回 1，否则返回 0

### KEYS
根据 pattern 返回匹配的键列表

### RANDOMKEY
从当前数据库中随机返回一个 key

### RENAME
将 key 重命名为 newkey

### RENAMENX
只有当 newkey 不存在时，才将 key 重命名为 newkey

### EXPIRE
为 key 设置过期时间（秒）

### EXPIREAT
将 key 的过期时间设置为指定的 UNIX 时间戳

### TTL
返回 key 的剩余过期时间（秒）
