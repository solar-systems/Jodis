package cn.abelib.jodis.utils;


import java.util.List;
import java.util.Map;
import java.util.Objects;
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
}
