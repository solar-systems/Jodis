package cn.abelib.jodis.impl;


/**
 * @Author: abel.huang
 * @Date: 2020-07-02 22:42
 * 设置超时
 */
public interface ExpireObject {
    long created();

    void created(long createTime);

    long ttl();

    void ttl(long ttl);
    
    /**
     * 获取过期时间戳
     * @return 过期时间戳（毫秒），永不过期返回 -1
     */
    long getExpireTime();
    
    /**
     * 设置过期时间戳
     * @param expireTime 过期时间戳（毫秒）
     */
    void setExpireTime(long expireTime);
}
