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

### SCAN
增量迭代数据库中的 key，避免 KEYS 命令阻塞服务器

**语法：** `SCAN cursor [MATCH pattern] [COUNT count]`

**参数：**
- `cursor`: 游标，从 "0" 开始
- `MATCH pattern` (可选): 匹配模式，支持 `*` 和 `?` 通配符
- `COUNT count` (可选): 每次返回的元素数量建议值，默认 10

**返回值：** 数组 `[next_cursor, key1, key2, ...]`，当 next_cursor 为 "0" 时表示迭代结束

**示例：**
```java
// 迭代所有 key
String cursor = "0";
do {
    List<String> result = client.scan(cursor);
    cursor = result.get(0); // 获取下一个游标
    for (int i = 1; i < result.size(); i++) {
        System.out.println("Found key: " + result.get(i));
    }
} while (!cursor.equals("0"));

// 只扫描 user:* 模式的 key
List<String> result = client.scan("0", "user:*");

// 指定每次返回 100 个元素
List<String> result = client.scan("0", "order:*", 100);
```

**注意：**
- SCAN 是增量迭代，不会一次性返回所有结果
- 过期的 key 不会被返回
- COUNT 是建议值，实际返回数量可能略有不同
