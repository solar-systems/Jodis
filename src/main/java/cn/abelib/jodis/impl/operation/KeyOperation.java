package cn.abelib.jodis.impl.operation;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.impl.JodisObject;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
public class KeyOperation extends AbstractOperation {
    public KeyOperation(JodisDb jodisDb) {
        super(jodisDb);
    }

    public JodisObject jodisObject(String key) {
        return this.jodisDb.get(key);
    }

    public int size() {
        return jodisDb.size();
    }

    /**
     * Redis command: TYPE
     * @param key
     * @return
     */
    public String type(String key) {
        if (!exists(key)) {
            return null;
        }
        return jodisObject(key).type();
    }

    /**
     * 如果集合为空直接删除
     * Redis command: DEL
     * @param key
     * @return
     */
    public int delete(String key) {
        if (this.jodisDb.containsKey(key)) {
            this.jodisDb.remove(key);
            return 1;
        }
        return 0;
    }

    /**
     * Redis command: EXISTS
     * 检查 key 是否存在且未过期
     * @param key
     * @return
     */
    public boolean exists(String key) {
        JodisObject obj = this.jodisDb.get(key);
        if (obj == null) {
            return false;
        }
        
        // 检查是否过期
        if (obj.ttl() > 0) {
            long now = System.currentTimeMillis();
            if (now >= obj.created() + obj.ttl()) {
                // 已过期，删除并返回 false
                this.jodisDb.remove(key);
                return false;
            }
        }
        return true;
    }

    /**
     * Redis command: EXPIRE
     * 为 key 设置以秒为单位的生存时间
     * @param key
     * @param seconds 秒数
     * @return 1 成功，0 失败
     */
    public int expire(String key, long seconds, TimeUnit unit) {
        JodisObject obj = this.jodisDb.get(key);
        if (obj == null) {
            return 0; // key 不存在
        }
        
        // 将秒转换为毫秒
        long ttlMillis = unit.toMillis(seconds);
        if (ttlMillis <= 0) {
            // TTL 为 0 或负数，立即删除 key
            delete(key);
            return 1;
        }
        
        // 设置 TTL（保持原有的 created 时间）
        obj.ttl(ttlMillis);
        return 1;
    }

    /**
     * Redis command: EXPIREAT
     * 为 key 设置以时间戳为单位的过期时间
     * @param key
     * @param timestamp Unix 时间戳（秒）
     * @return 1 成功，0 失败
     */
    public int expireAt(String key, long timestamp, TimeUnit unit) {
        JodisObject obj = this.jodisDb.get(key);
        if (obj == null) {
            return 0; // key 不存在
        }
        
        // 将时间戳转换为毫秒
        long timestampMillis = unit.toMillis(timestamp);
        long now = System.currentTimeMillis();
        
        if (timestampMillis <= now) {
            // 过期时间已过，立即删除 key
            delete(key);
            return 1;
        }
        
        // 计算剩余的 TTL
        long ttl = timestampMillis - now;
        obj.ttl(ttl);
        return 1;
    }

    /**
     * Redis command: KEYS
     * @param pattern
     * @return
     */
    public List<String> keys(String pattern) {
        if (StringUtils.equals(StringUtils.STAR, pattern)) {
            return new ArrayList<>(this.jodisDb.keySet());
        }
        
        // 将 Redis 通配符模式转换为 Java 正则表达式
        // * 匹配任意数量的字符
        // ? 匹配单个字符
        String regex = pattern
                .replaceAll("\\.", "\\\\.")  // 转义点号
                .replaceAll("\\*", ".*")      // * -> .*
                .replaceAll("\\?", ".");      // ? -> .
        String finalRegex = "^" + regex + "$";           // 确保完全匹配
        
        return this.jodisDb.keySet()
                .stream()
                .filter(key -> key.matches(finalRegex))
                .collect(Collectors.toList());
    }

    /**
     * Redis command: TTL
     * 以秒为单位返回 key 的剩余生存时间
     * @param key
     * @return 剩余秒数；key 不存在或已过期返回 -2；没有设置过期时间返回 -1
     */
    public int ttl(String key) {
        JodisObject obj = this.jodisDb.get(key);
        if (obj == null) {
            return -2; // key 不存在
        }
        
        // 检查是否设置了过期时间
        if (obj.ttl() <= 0) {
            return -1; // 没有设置过期时间
        }
        
        // 计算剩余时间（毫秒转秒）
        long now = System.currentTimeMillis();
        long expireTime = obj.created() + obj.ttl();
        long remaining = expireTime - now;
        
        if (remaining <= 0) {
            // 已过期，删除 key
            delete(key);
            return -2;
        }
        
        return (int) Math.max(0, remaining / 1000);
    }

    /**
     * Redis command: RENAME
     * @param key
     * @param newKey
     */
    public boolean rename(String key, String newKey) {
        if (StringUtils.equals(key, newKey) || !exists(key)) {
            return false;
        }
        JodisObject value = jodisObject(key);
        delete(key);
        this.jodisDb.put(newKey, value);
        return true;
    }

    /**
     * Redis command: RENAMENX
     * @param key
     * @param newKey
     */
    public boolean renameIfNotExist(String key, String newKey) {
        if (exists(newKey)) {
            return false;
        }
        return rename(key, newKey);
    }

    /**
     * Redis command: RANDOMKEY
     * @return
     */
    public String randomKey(){
        if (size() > 0) {
            return this.jodisDb.keySet().iterator().next();
        }
        return null;
    }

    /**
     * Redis command: SCAN
     * SCAN cursor [MATCH pattern] [COUNT count]
     * 
     * @param cursor 游标，从上一次迭代的位置开始
     * @param pattern 匹配模式（可选），支持通配符 * 和 ?
     * @param count 每次返回的元素数量建议值（可选），默认 10
     * @return [nextCursor, key1, key2, ...]，当 nextCursor 为 "0" 时表示迭代结束
     */
    public List<String> scan(String cursor, String pattern, int count) {
        // 解析游标
        int currentCursor;
        try {
            currentCursor = Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            currentCursor = 0;
        }
        
        // 获取所有 key
        List<String> allKeys = new ArrayList<>(this.jodisDb.keySet());
        int totalSize = allKeys.size();
        
        // 如果游标已经超出范围，返回 0 表示结束
        if (currentCursor >= totalSize) {
            return Lists.newArrayList("0");
        }
        
        // 收集元素
        List<String> result = Lists.newArrayList();
        int nextCursor = currentCursor;
        int collected = 0;
        
        // 从当前游标位置开始遍历
        while (nextCursor < totalSize && collected < count) {
            String key = allKeys.get(nextCursor);
            
            // 检查是否过期，过期的 key 不返回
            if (!exists(key)) {
                nextCursor++;
                continue;
            }
            
            // 如果有 pattern，进行匹配
            if (pattern != null && !StringUtils.equals(StringUtils.STAR, pattern)) {
                // 将 Redis 通配符模式转换为 Java 正则表达式
                String regex = pattern
                        .replaceAll("\\.", "\\\\.")  // 转义点号
                        .replaceAll("\\*", ".*")      // * -> .*
                        .replaceAll("\\?", ".");      // ? -> .
                regex = "^" + regex + "$";           // 确保完全匹配
                
                if (!key.matches(regex)) {
                    nextCursor++;
                    continue;
                }
            }
            
            // 匹配成功，添加到结果
            result.add(key);
            collected++;
            nextCursor++;
        }
        
        // 构建返回值：[nextCursor, key1, key2, ...]
        List<String> response = Lists.newArrayListWithCapacity(result.size() + 1);
        response.add(String.valueOf(nextCursor));
        response.addAll(result);
        
        return response;
    }
}
