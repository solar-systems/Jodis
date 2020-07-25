package cn.abelib.jodis.impl.executor;

import cn.abelib.jodis.impl.JodisDb;

/**
 * @Author: abel.huang
 * @Date: 2020-07-16 01:36
 */
public class ExecutorFactory {
    private JodisDb jodisDb;

    public ExecutorFactory(JodisDb jodisDb) {
        this.jodisDb = jodisDb;
    }

    public Executor stringExecutor() {
        return new StringExecutor(jodisDb);
    }

    public Executor listExecutor() {
        return new ListExecutor(jodisDb);
    }

    public Executor setExecutor() {
        return new SetExecutor(jodisDb);
    }

    public Executor hashExecutor() {
        return new HashExecutor(jodisDb);
    }

    public Executor zSetExecutor() {
        return new SortedSetExecutor(jodisDb);
    }
}
