package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.util.*;
import java.util.function.Function;

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
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Map
     * */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> parse(Object object, Class<K> kClass, Class<V> vClass) {
        Map<?, ?> map = parse(object);
        if (map == null || kClass == null || vClass == null) {
            return null;
        }
        Map<K, V> result;
        try {
            result = ObjectUtil.newInstance(map.getClass());
        } catch (Exception e) {
            result = new LinkedHashMap<>();
        }
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            result.put(ObjectUtil.parse(key, kClass), ObjectUtil.parse(map.get(key), vClass));
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
    public static Map<?, ?> parse(Object object) {
        return ObjectUtil.parse(object, Map.class);
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
    public static <T> T get(Map<?, ?> map, Object key, Class<T> tClass) {
        if (map == null) {
            return null;
        }
        Object o = map.get(key);
        return ObjectUtil.parse(o, tClass);
    }

    /**
     * 获取map
     *
     * @param map map
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Map
     * */
    public static <K, V> Map<K, V> getMap(Map<?, ?> map, Object key, Class<K> kClass, Class<V> vClass) {
        return parse(get(map, key, Map.class), kClass, vClass);
    }

    /**
     * 获取Collection<Map<K, V>>
     *
     * @param map map
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Collection<Map<K, V>>
     * */
    public static <K, V> Collection<Map<K, V>> getCollectionMap(Map<?, ?> map, Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseMap(get(map, key, Collection.class), kClass, vClass);
    }

    /**
     * 获取List<Map<K, V>>
     *
     * @param map map
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return List<Map<K, V>>
     * */
    public static <K, V> List<Map<K, V>> getListMap(Map<?, ?> map, Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseListMap(get(map, key, List.class), kClass, vClass);
    }

    /**
     * 获取Set<Map<K, V>>
     *
     * @param map map
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Set<Map<K, V>>
     * */
    public static <K, V> Set<Map<K, V>> getSetMap(Map<?, ?> map, Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseSetMap(get(map, key, Set.class), kClass, vClass);
    }

    /**
     * key处理
     *
     * @param map map
     * @param action action
     * @param <KEY> KEY
     * @param <VALUE> VALUE
     *
     * @return map
     * */
    @SuppressWarnings("unchecked")
    public static <KEY, VALUE> Map<KEY, VALUE> keyHandler(Map<KEY, VALUE> map, Function<KEY, KEY> action) {
        if (ObjectUtil.isEmpty(map) || action == null) {
            return map;
        }
        Map<KEY, VALUE> result;
        try {
            result = ObjectUtil.newInstance(map.getClass());
        } catch (Exception e) {
            throw new BaseException(e);
        }
        for (KEY key : map.keySet()) {
            KEY newKey = action.apply(key);
            if (newKey == null) {
                continue;
            }
            result.put(newKey, map.get(key));
        }
        return result;
    }

    /**
     * value处理
     *
     * @param map map
     * @param action action
     * @param <KEY> KEY
     * @param <VALUE> VALUE
     *
     * @return map
     * */
    @SuppressWarnings("unchecked")
    public static <KEY, VALUE> Map<KEY, VALUE> valueHandler(Map<KEY, VALUE> map, Function<VALUE, VALUE> action) {
        if (ObjectUtil.isEmpty(map) || action == null) {
            return map;
        }
        Map<KEY, VALUE> result;
        try {
            result = ObjectUtil.newInstance(map.getClass());
        } catch (Exception e) {
            throw new BaseException(e);
        }
        for (KEY key : map.keySet()) {
            result.put(key, action.apply(map.get(key)));
        }
        return result;
    }

    /**
     * 生成map
     *
     * @param result result
     * @param entryList entryList
     * @param <K> K
     * @param <V> V
     *
     * @return map
     * */
    @SafeVarargs
    public static <K, V> Map<K, V> asMap(Map<K, V> result, Map.Entry<K, V>... entryList) {
        if (entryList == null) {
            return null;
        }
        if (result == null) {
            result = new LinkedHashMap<>();
        }
        for (Map.Entry<K, V> entry : entryList) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 生成map
     *
     * @param <K> K
     * @param <V> V
     * @param entryList entryList
     *
     * @return map
     * */
    @SafeVarargs
    public static <K, V> Map<K, V> asMap(Map.Entry<K, V>... entryList) {
        return asMap(null, entryList);
    }

}