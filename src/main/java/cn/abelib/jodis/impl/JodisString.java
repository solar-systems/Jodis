package cn.abelib.jodis.impl;

import cn.abelib.jodis.api.JodisObject;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author abel.huang
 * @date 2020/6/30 17:42
 */
@Data
@AllArgsConstructor
public class JodisString implements JodisObject {
    private String holder;

    public String getHolder() {
        return holder;
    }
}
