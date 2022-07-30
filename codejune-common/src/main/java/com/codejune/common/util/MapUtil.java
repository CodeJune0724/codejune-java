package com.codejune.common.util;

import com.codejune.common.handler.KeyHandler;
import java.util.*;

/**
 * MapUtil
 *
 * @author ZJ
 * */
public final class MapUtil {

    /**
     * 将map转成object
     *
     * @param <T> T
     * @param map map
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T parse(Map<?, ?> map, Class<T> tClass) {
        return ObjectUtil.parse(map, tClass);
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
        return ObjectUtil.parse(o, tClass);
    }

    /**
     * 转换key
     *
     * @param map map
     * @param keyHandler keyHandler
     * @param <T> T
     *
     * @return Map
     * */
    public static <T> Map<?, T> transformKey(Map<?, T> map, KeyHandler keyHandler) {
        if (map == null) {
            return null;
        }
        if (keyHandler == null) {
            keyHandler = new KeyHandler() {};
        }
        Map<Object, T> result = new HashMap<>();
        Set<?> keySet = new HashSet<>(map.keySet());
        for (Object key : keySet) {
            Object newKey = keyHandler.getNewKey(key);
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
    public static <T, E> Map<T, E> parseToGeneric(Map<?, ?> map, Class<T> tClass, Class<E> eClass) {
        if (map == null || tClass == null || eClass == null) {
            return null;
        }
        Map<T, E> result = new LinkedHashMap<>();
        Map<Object, Object> parse = (Map<Object, Object>) map;
        Set<Object> keySet = parse.keySet();
        for (Object key : keySet) {
            result.put(ObjectUtil.parse(key, tClass), ObjectUtil.parse(parse.get(key), eClass));
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
        KeyHandler keyHandler = new KeyHandler() {
            @Override
            public Object getNewKey(Object key) {
                return StringUtil.underlineToHump(ObjectUtil.toString(key));
            }
        };
        return (Map<String, T>) transformKey(map, keyHandler);
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
        KeyHandler keyHandler = new KeyHandler() {
            @Override
            public Object getNewKey(Object key) {
                return StringUtil.humpToUnderline(ObjectUtil.toString(key));
            }
        };
        return (Map<String, T>) transformKey(map, keyHandler);
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