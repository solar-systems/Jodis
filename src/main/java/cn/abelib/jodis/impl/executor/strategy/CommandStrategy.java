package cn.abelib.jodis.impl.executor.strategy;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Response;

import java.util.List;

/**
 * 命令处理策略接口
 * @Author: abel.huang
 * @Date: 2026-03-21
 */
public interface CommandStrategy {
    /**
     * 执行具体命令逻辑
     * @param jodisDb 数据库实例
     * @param args 命令参数（不包含命令名）
     * @return 执行结果
     */
    Response execute(JodisDb jodisDb, List<String> args);
    
    /**
     * 获取必需参数数量
     * @return 必需参数数量，-1 表示不限制
     */
    default int getRequiredArgCount() {
        return -1;
    }
}
