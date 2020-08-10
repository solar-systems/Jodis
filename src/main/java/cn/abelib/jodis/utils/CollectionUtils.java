package cn.abelib.jodis.utils;


import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: abel.huang
 * @Date: 2020-07-12 23:14
 */
public class CollectionUtils {

    private CollectionUtils() {}

    public static Map deepCopyMap(Map<?, ?> map) {
        if (isEmptyMap(map)) {
            return map;
        }
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static boolean isEmptyMap(Map<?, ?> map) {
        if (Objects.isNull(map)) {
            return true;
        }
        return map.size() == 0;
    }

    public static int listIndex(List<String> list, String value) {
        for (int i = 0; i < list.size(); i ++) {
            if (StringUtils.equals(list.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 集合差集
     * @return
     */
    public static Set<String> diffSet(Set<String> set1, Set<String> set2){
        Set<String> result = Sets.newHashSet();
        result.clear();
        result.addAll(set1);
        result.removeAll(set2);
        return result;
    }

    /**
     * 集合并集
     * @return
     */
    public static Set<String> unionSet(Set<String> set1, Set<String> set2){
        Set<String> result = Sets.newHashSet();
        result.clear();
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }

    /**
     * 集合交集
     * @return
     */
    public static Set<String> interSet(Set<String> set1, Set<String> set2){
        Set<String> result = Sets.newHashSet();
        result.clear();
        result.addAll(set1);
        result.retainAll(set2);
        return result;
    }
}
