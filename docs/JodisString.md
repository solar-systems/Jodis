## JodisString

### GET
获取指定 key 的值

### SET
设置指定 key 的值

### GETSET
设置指定 key 的新值，并返回 key 的旧值

### GETRANGE
返回 key 中字符串的子串，由偏移量 start 和 end 指定范围

### MGET
批量获取多个 key 的值

### MSET
批量设置多个 key 的值

### SETEX
设置 key 的值，并指定过期时间（秒）

### SETNX
只有当 key 不存在时，才设置 key 的值

### SETRANGE
用 value 参数覆写（或更新）key 所存储的字符串值，从 offset 指定的索引开始

### STRLEN
返回 key 所存储的字符串值的长度

### INCR
将 key 中储存的数字值增一

### INCRBY
将 key 所储存的值加上增量 increment

### INCRBYFLOAT
为 key 中所储存的值加上浮点数增量

### DECR
将 key 中储存的数字值减一

### DECRBY
命令用于将 key 所储存的值减去减量 decrement

### APPEND
如果 key 已经存在并且是一个字符串，APPEND 命令将把 value 追加到 key 原来的值的末尾