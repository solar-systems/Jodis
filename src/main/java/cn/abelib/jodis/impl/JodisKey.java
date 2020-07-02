package cn.abelib.jodis.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * @author abel.huang
 * @date 2020/6/30 17:44
 */
@Data
@EqualsAndHashCode(of = {"key"})
public class JodisKey {
    private String key;

    private long createTime = Instant.now().toEpochMilli();

    private long lease = -1;
}
