package com.codejune.common.util;

import com.codejune.common.Action;
import java.util.*;

/**
 * MapUtil
 *
 * @author ZJ
 * */
public final class MapUtil {

    /**
     * 转成Map
     *
     * @param object object
     * @param tClass tClass
     * @param eClass eClass
     * @param <T> T
     * @param <E> E
     *
     * @return Map
     * */
    public static <T, E> Map<T, E> parse(Object object, Class<T> tClass, Class<E> eClass) {
        return transformGeneric(parse(object), tClass, eClass);
    }

    /**
     * 转成Map
     *
     * @param object object
     *
     * @return Map
     * */
    public static Map<?,?> parse(Object object) {
        return ObjectUtil.transform(object, Map.class);
    }

    /**
     * 将map转成object
     *
     * @param <T> T
     * @param map map
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T transform(Map<?, ?> map, Class<T> tClass) {
        return ObjectUtil.transform(map, tClass);
    }

    /**
     * 获取值
     *
     * @param <T> T
     * @param map map
     * @param key key
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T getValue(Map<?, ?> map, String key, Class<T> tClass) {
        if (map == null) {
            return null;
        }
        Object o = map.get(key);
        return ObjectUtil.transform(o, tClass);
    }

    /**
     * 转换key
     *
     * @param map map
     * @param action action
     * @param <T> T
     *
     * @return Map
     * */
    public static <T> Map<?, T> transformKey(Map<?, T> map, Action<Object, Object> action) {
        if (map == null) {
            return null;
        }
        if (action == null) {
            action = o -> null;
        }
        Map<Object, T> result = new HashMap<>();
        Set<?> keySet = new HashSet<>(map.keySet());
        for (Object key : keySet) {
            Object newKey = action.then(key);
            if (newKey == null) {
                continue;
            }
            result.put(newKey, map.get(key));
        }
        return result;
    }

    /**
     * 转成指定泛型的Map
     *
     * @param map map
     * @param tClass tClass
     * @param eClass eClass
     * @param <T> T
     * @param <E> E
     *
     * @return Map
     * */
    @SuppressWarnings("unchecked")
    public static <T, E> Map<T, E> transformGeneric(Map<?, ?> map, Class<T> tClass, Class<E> eClass) {
        if (map == null || tClass == null || eClass == null) {
            return null;
        }
        Map<T, E> result = new LinkedHashMap<>();
        Map<Object, Object> parse = (Map<Object, Object>) map;
        Set<Object> keySet = parse.keySet();
        for (Object key : keySet) {
            result.put(ObjectUtil.transform(key, tClass), ObjectUtil.transform(parse.get(key), eClass));
        }
        return result;
    }

    /**
     * 将key转成驼峰
     *
     * @param map map
     * @param <T> T
     *
     * @return map
     * */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> transformKeyToHump(Map<String, T> map) {
        Action<Object, Object> action = key -> StringUtil.underlineToHump(ObjectUtil.toString(key));
        return (Map<String, T>) transformKey(map, action);
    }

    /**
     * 将key转成下划线
     *
     * @param map map
     * @param <T> T
     *
     * @return map
     * */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> transformKeyToUnderline(Map<String, T> map) {
        Action<Object, Object> action = key -> StringUtil.humpToUnderline(ObjectUtil.toString(key));
        return (Map<String, T>) transformKey(map, action);
    }

    /**
     * 过滤key
     *
     * @param map map
     * @param tCollection tCollection
     * @param <T> T
     * @param <E> E
     *
     * @return map
     * */
    public static <T, E> Map<T, E> filterKey(Map<T, E> map, Collection<T> tCollection) {
        if (ObjectUtil.isEmpty(map) || ObjectUtil.isEmpty(tCollection)) {
            return map;
        }
        Map<T, E> result = new LinkedHashMap<>();
        Set<T> keySet = map.keySet();
        for (T key : keySet) {
            if (tCollection.contains(key)) {
                result.put(key, map.get(key));
            }
        }
        return result;
    }

}