package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.ExpireObject;

/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:07
 */
public class ExpireJodisList extends JodisList implements ExpireObject {


    @Override
    public long created() {
        return 0;
    }

    @Override
    public void created(long createTime) {

    }

    @Override
    public long ttl() {
        return 0;
    }

    @Override
    public void ttl(long ttl) {

    }
}
