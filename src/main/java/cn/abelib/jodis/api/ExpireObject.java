package cn.abelib.jodis.api;

import cn.abelib.jodis.api.JodisObject;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 22:42
 * 设置超时
 */
public interface ExpireObject extends JodisObject {
    long created();

    void created(long createTime);

    long ttl();

    void ttl(long ttl);
}
