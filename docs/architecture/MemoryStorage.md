# 内存存储结构

Jodis 采用纯内存存储，所有数据保存在内存中，提供微秒级的访问速度。

## 核心数据结构

### 1. ConcurrentHashMap

**作用**：存储所有键值对的主数据结构。

```java
public class JodisDb {
    // 主存储结构
    private final ConcurrentHashMap<String, JodisObject> jodisCollection;
    
    public JodisDb() {
        this.jodisCollection = new ConcurrentHashMap<>();
    }
    
    public JodisObject put(String key, JodisObject value) {
        return jodisCollection.put(key, value);
    }
    
    public JodisObject get(String key) {
        return jodisCollection.get(key);
    }
    
    public boolean containsKey(String key) {
        return jodisCollection.containsKey(key);
    }
}
```

**特点**：
- ✅ 线程安全，无需额外同步
- ✅ 高并发性能优秀
- ✅ 支持 ConcurrentMap 的所有操作

### 2. JodisObject

**作用**：统一封装各种数据类型的对象。

```java
public class JodisObject implements ExpireObject {
    private Object value;          // 实际数据（JodisString/JodisList/等）
    private String type;           // 数据类型标识
    private String encoding;       // 编码方式
    private long created;          // 创建时间戳
    private long ttl;              // 生存时间（毫秒），-1 表示永不过期
    
    // Getter/Setter
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    
    public String type() { return type; }
    
    public long ttl() { return ttl; }
    public void ttl(long ttl) { this.ttl = ttl; }
    
    public long getExpireTime() {
        if (this.ttl <= 0) {
            return -1;  // 永不过期
        }
        return this.created + this.ttl;
    }
}
```

**工厂方法**：
```java
// 创建 String 类型对象
JodisObject stringObj = JodisObject.putJodisString("hello");

// 创建 List 类型对象
JodisObject listObj = JodisObject.putJodisList(Arrays.asList("a", "b"));

// 创建 Hash 类型对象
JodisObject hashObj = JodisObject.putJodisHash(new HashMap<>());
```

### 3. ExpireObject 接口

**作用**：定义过期时间的契约。

```java
public interface ExpireObject {
    /**
     * 获取 TTL（毫秒）
     */
    long ttl();
    
    /**
     * 设置 TTL
     */
    void ttl(long ttl);
    
    /**
     * 获取过期时间戳
     */
    long getExpireTime();
}
```

## 数据类型底层实现

### 1. String - JodisString

```java
public class JodisString {
    private String holder;  // 实际字符串
    
    public JodisString(String holder) {
        this.holder = holder;
    }
    
    public String getHolder() {
        return holder;
    }
}
```

**使用场景**：
- 缓存字符串
- 计数器（INCR/DECR）
- 分布式锁（SETNX）

### 2. List - JodisList

```java
public class JodisList {
    private List<String> holder;  // LinkedList 实现
    
    public JodisList(List<String> holder) {
        this.holder = holder;
    }
    
    public List<String> getHolder() {
        return holder;
    }
    
    public void addFirst(String item) {
        ((LinkedList<String>) holder).addFirst(item);
    }
    
    public void addLast(String item) {
        ((LinkedList<String>) holder).addLast(item);
    }
}
```

**使用场景**：
- 消息队列（LPUSH/RPOP）
- 最新 N 条记录（LRANGE）

### 3. Hash - JodisHash

```java
public class JodisHash {
    private Map<String, String> holder;  // HashMap 实现
    
    public JodisHash(Map<String, String> holder) {
        this.holder = holder;
    }
    
    public Map<String, String> getHolder() {
        return holder;
    }
    
    public void put(String field, String value) {
        holder.put(field, value);
    }
    
    public String get(String field) {
        return holder.get(field);
    }
}
```

**使用场景**：
- 存储对象（用户信息、商品详情）
- 部分字段更新（HSET/HGET）

### 4. Set - JodisSet

```java
public class JodisSet {
    private Set<String> holder;  // HashSet 实现
    
    public JodisSet(Set<String> holder) {
        this.holder = holder;
    }
    
    public Set<String> getHolder() {
        return holder;
    }
    
    public boolean add(String member) {
        return holder.add(member);
    }
    
    public boolean remove(String member) {
        return holder.remove(member);
    }
}
```

**使用场景**：
- 去重集合（标签、好友关系）
- 集合运算（交集、并集）

### 5. SortedSet - JodisSortedSet

```java
public class JodisSortedSet {
    private Map<String, Double> holder;  // member -> score 映射
    private SkipList skipList;           // 按 score 排序的跳跃表
    
    public JodisSortedSet(Map<String, Double> holder, SkipList skipList) {
        this.holder = holder;
        this.skipList = skipList;
    }
    
    public void add(String member, double score) {
        holder.put(member, score);
        skipList.add(score, member);
    }
    
    public List<String> range(int start, int end) {
        return skipList.range(start, end);
    }
}
```

**使用场景**：
- 排行榜（游戏分数、热度排名）
- 带权重的队列

## 跳跃表（SkipList）

### 数据结构

```java
public class SkipList {
    private SkipNode head;      // 头节点（哨兵）
    private int maxLevel;       // 最大层数（固定为 32）
    private int currentMaxLevel; // 当前使用的最大层数
    private int length;         // 节点数量
    
    public static class SkipNode {
        double score;                    // 分数
        String member;                   // 成员
        SkipNode[] forward;             // 向前指针数组
        
        public SkipNode(int level, double score, String member) {
            this.forward = new SkipNode[level];
            this.score = score;
            this.member = member;
        }
    }
}
```

### 插入操作

```java
public void add(double score, String member) {
    SkipNode[] update = new SkipNode[maxLevel];
    SkipNode current = head;
    
    // 1. 从顶层开始查找插入位置
    for (int i = currentMaxLevel - 1; i >= 0; i--) {
        while (current.forward[i] != null && 
               current.forward[i].score < score) {
            current = current.forward[i];
        }
        update[i] = current;
    }
    
    // 2. 移动到底层，检查是否已存在
    current = current.forward[0];
    if (current != null && current.score == score) {
        current.member = member;  // 更新已有成员
        return;
    }
    
    // 3. 随机生成新节点的层数
    int newLevel = randomLevel();
    SkipNode newNode = new SkipNode(newLevel, score, member);
    
    // 4. 如果新层数超过当前最大层数，需要更新
    if (newLevel > currentMaxLevel) {
        for (int i = currentMaxLevel; i < newLevel; i++) {
            update[i] = head;
        }
        currentMaxLevel = newLevel;
    }
    
    // 5. 在各层插入新节点
    for (int i = 0; i < newLevel; i++) {
        newNode.forward[i] = update[i].forward[i];
        update[i].forward[i] = newNode;
    }
    
    length++;
}

/**
 * 随机生成层数（1-32）
 * 层数越高，概率越低
 */
private int randomLevel() {
    int level = 1;
    Random random = new Random();
    
    while (random.nextDouble() < 0.25 && level < maxLevel) {
        level++;
    }
    
    return level;
}
```

### 查询操作

```java
public String search(double score) {
    SkipNode current = head;
    
    // 从顶层开始查找
    for (int i = currentMaxLevel - 1; i >= 0; i--) {
        while (current.forward[i] != null && 
               current.forward[i].score <= score) {
            current = current.forward[i];
            
            if (current.score == score) {
                return current.member;
            }
        }
    }
    
    return null;  // 未找到
}
```

**复杂度分析**：
- 平均时间复杂度：O(log n)
- 空间复杂度：O(n)
- 查找性能接近平衡二叉树

## 过期时间管理

### 惰性删除策略

```java
public class KeyOperation {
    
    /**
     * 检查 key 是否存在且未过期
     */
    public boolean exists(String key) {
        JodisObject obj = jodisDb.get(key);
        if (obj == null) {
            return false;
        }
        
        // 检查是否过期
        if (obj.ttl() > 0) {
            long now = System.currentTimeMillis();
            if (now >= obj.created() + obj.ttl()) {
                // 已过期，删除并返回 false
                jodisDb.remove(key);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 设置过期时间
     */
    public int expire(String key, long seconds, TimeUnit unit) {
        JodisObject obj = jodisDb.get(key);
        if (obj == null) {
            return 0;  // key 不存在
        }
        
        long ttlMillis = unit.toMillis(seconds);
        if (ttlMillis <= 0) {
            delete(key);  // TTL 为 0 或负数，立即删除
            return 1;
        }
        
        obj.ttl(ttlMillis);
        return 1;
    }
}
```

**优势**：
- ✅ 降低 CPU 开销（不需要定时扫描）
- ✅ 只在访问时检查过期
- ✅ 实现简单高效

**劣势**：
- ❌ 过期的 key 可能长时间不被删除
- ❌ 占用内存空间

## 并发控制

### 线程安全性

```java
// ConcurrentHashMap 保证线程安全
ConcurrentHashMap<String, JodisObject> data = new ConcurrentHashMap<>();

// 多个线程可以并发访问不同的 key
data.put("key1", value1);  // 线程 A
data.put("key2", value2);  // 线程 B
data.get("key1");          // 线程 C
```

### 原子操作

```java
// INCR 操作的原子性
public int incrementBy(String key, int incrAmount) {
    synchronized (this) {  // 或使用更细粒度的锁
        String value = get(key);
        int newValue = Integer.parseInt(value) + incrAmount;
        set(key, String.valueOf(newValue));
        return newValue;
    }
}
```

## 相关文件

- [系统架构](SystemArchitecture.md)
- [命令执行框架](CommandExecutor.md)
- [持久化机制](PersistenceLayer.md)
