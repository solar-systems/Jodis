package cn.abelib.jodis.internals;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: abel.huang
 * @Date: 2020-07-06 23:51
 */
public class SkipList {
    private static final double DEFAULT_SKIP_LIST_P = 0.5;
    private static final int DEFAULT_MAX_LEVEL = 8;

    private class SkipNode {
        private double score;
        private SkipNode[] next = new SkipNode[DEFAULT_MAX_LEVEL];
        private int level;
        private String value;

        SkipNode() {
            this(0, 0, null);
        }

        SkipNode(double score, int level, String value) {
            this.score = score;
            this.level = level;
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{ score: ");
            builder.append(score);
            builder.append("; level: ");
            builder.append(level);
            builder.append("; value: ");
            builder.append(value);
            builder.append(" }");

            return builder.toString();
        }
    }

    /**
     * 设定的跳表最大高度
     */
    private int maxLevel;
    /**
     * 概率
     */
    private double probability;
    /**
     * 跳表当前最大高度
     */
    private int currentMaxLevel;

    /**
     * 跳表长度
     */
    private int length;

    /**
     * 跳表头结点
     */
    private SkipNode head;

    public SkipList() {
        this(DEFAULT_MAX_LEVEL, DEFAULT_SKIP_LIST_P);
    }

    public SkipList(int maxLevel, double probability) {
        this.maxLevel = maxLevel;
        this.probability = probability;
        this.currentMaxLevel = 1;
        head = new SkipNode();
        length = 0;
    }

    public void add(double score, String value) {
        int level = randomLevel();
        SkipNode newNode = new SkipNode(score, level, value);

        SkipNode node = head;
        SkipNode[] update = findNodeNext(node, level, score);

        // 给新插入节点设置前后关系
        for (int i = 0; i < level; i ++) {
            newNode.next[i] = update[i].next[i];
            update[i].next[i] = newNode;
        }
        //检测是否需要更新跳表目前索引高度
        currentMaxLevel = Math.max(currentMaxLevel, level);
        length++;
    }

    public String find(double score) {
        SkipNode node = head;
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            while (node.next[i] != null && node.next[i].score < score) {
                node = node.next[i];
            }
        }
        // 找到该数据
        if (node.next[0] != null && node.next[0].score == score) {
            return  node.next[0].value;
        }
        return null;
    }

    public void delete(double score) {
        SkipNode node = head;
        /**
         * 找到当前结点的全部下一个节点
         */
        SkipNode[] update = findNodeNext(node, currentMaxLevel, score);
        node = update[0];
        if (node.next[0] != null && node.next[0].score == score) {
            for (int i = currentMaxLevel - 1; i >= 0; i--) {
                if (update[i].next[i] != null && update[i].next[i].score == score) {
                    update[i].next[i] = update[i].next[i].next[i];
                }
            }
        }

        // 更新currentMaxLevel
        while (currentMaxLevel > 1 && head.next[currentMaxLevel] == null){
            currentMaxLevel--;
        }
        length--;
    }

    public int size() {
        return this.length;
    }

    private SkipNode[] findNodeNext(SkipNode node, int level, double score){
        SkipNode[] update = new SkipNode[level];
        /**
         * 找到当前结点的全部下一个节点
         */
        for (int i = level - 1; i >= 0; i--) {
            while (node.next[i] != null && node.next[i].score < score) {
                node = node.next[i];
            }
            update[i] = node;
        }
        return update;
    }

    /**
     * 对于每一个新插入的节点，都需要调用 randomLevel 生成一个合理的层数。
     *  该 randomLevel 方法会随机生成 1 ~ MAX_LEVEL 之间的数，且 ：
     *  1-p 的概率返回 1
     *  p *（1-p) 的概率返回 2
     *  p^2 *（1-p)的概率返回 3
     *  p^(n - 1) *（1-p)的概率返回 n
     *
     *  其中Math.random() 返回大于等于 0.0 且小于 1.0 的伪随机数
     * @return
     */
    private int randomLevel() {
        int level = 1;

        while (Math.random() < DEFAULT_SKIP_LIST_P && level < DEFAULT_MAX_LEVEL) {
            level += 1;
        }
        return level;
    }

    public List<String> toList() {
        SkipNode node = head;
        List<String> result = new ArrayList<>(length);
        while (node.next[0] != null) {
            node = node.next[0];
            result.add(node.value);
        }
        return result;
    }
}

