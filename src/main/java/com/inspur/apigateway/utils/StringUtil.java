package com.inspur.apigateway.utils;

import java.util.List;
import java.util.Map;

public class StringUtil {

    /**
     * 字符串为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 字符串不为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 队列为空
     *
     * @param list
     * @return
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    /**
     * Map为空
     *
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.size() == 0;
    }

    /**
     * Map不为空
     *
     * @param map
     * @return
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
