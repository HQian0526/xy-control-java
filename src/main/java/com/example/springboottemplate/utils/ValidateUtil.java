package com.example.springboottemplate.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 通用校验工具类
 */
public class ValidateUtil {
    /**
     * 检查集合是否为空（null 或 size=0）
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 检查数组是否为空（null 或 length=0）
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 检查 Map 是否为空（null 或 size=0）
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查字符串是否为空（null 或 空字符串）
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
