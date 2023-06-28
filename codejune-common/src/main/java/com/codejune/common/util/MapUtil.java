package com.codejune.common.util;

import com.codejune.common.Action;
import com.codejune.common.exception.InfoException;
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
    @SuppressWarnings("unchecked")
    public static <T, E> Map<T, E> parse(Object object, Class<T> tClass, Class<E> eClass) {
        Map<?, ?> map = parse(object);
        if (map == null || tClass == null || eClass == null) {
            return null;
        }
        Map<T, E> result;
        try {
            result = map.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            result = new LinkedHashMap<>();
        }
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            result.put(ObjectUtil.transform(key, tClass), ObjectUtil.transform(map.get(key), eClass));
        }
        return result;
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
     * key处理
     *
     * @param map map
     * @param action action
     * @param <KEY> KEY
     *
     * @return map
     * */
    @SuppressWarnings("unchecked")
    public static <KEY> Map<KEY, Object> keyHandler(Map<KEY, Object> map, Action<KEY, KEY> action) {
        if (ObjectUtil.isEmpty(map) || action == null) {
            return map;
        }
        Map<KEY, Object> result;
        try {
            result = map.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InfoException(e);
        }
        for (KEY key : map.keySet()) {
            KEY newKey = action.then(key);
            if (newKey == null) {
                continue;
            }
            result.put(newKey, map.get(key));
        }
        return result;
    }

}