package com.codejune;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.codejune.core.BaseException;
import com.codejune.core.Builder;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Json
 *
 * @author ZJ
 * */
public final class Json implements Builder {

    private Object data;

    public Json() {
        this.data = null;
    }

    public Json(Object data) {
        this.build(data);
    }

    /**
     * 获取
     *
     * @param key key
     *
     * @return Json
     * */
    public Json get(Object key) {
        return switch (this.data) {
            case Map<?, ?> map -> Json.parse(MapUtil.get(map, key, Object.class));
            case Collection<?> collection ->
                    Json.parse(ArrayUtil.get(ObjectUtil.parse(collection, List.class), ObjectUtil.parse(key, Integer.class), Object.class));
            case null, default -> new Json();
        };
    }

    /**
     * 获取
     *
     * @param key key
     * @param vClass vClass
     * @param <V> V
     *
     * @return V
     * */
    public <V> V get(Object key, Class<V> vClass) {
        return ObjectUtil.parse(get(key).data, vClass);
    }


    /**
     * 获取map
     *
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Map
     * */
    public <K, V> Map<K, V> getMap(Object key, Class<K> kClass, Class<V> vClass) {
        return MapUtil.parse(get(key, Map.class), kClass, vClass);
    }

    /**
     * 获取Collection
     *
     * @param key key
     * @param tClass tClass
     * @param <T> T
     *
     * @return Collection
     * */
    public <T> Collection<T> getCollection(Object key, Class<T> tClass) {
        return ArrayUtil.parse(this.get(key, Collection.class), tClass);
    }

    /**
     * 获取List
     *
     * @param key key
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public <T> List<T> getList(Object key, Class<T> tClass) {
        return ArrayUtil.parseList(this.get(key, List.class), tClass);
    }

    /**
     * 获取Set
     *
     * @param key key
     * @param tClass tClass
     * @param <T> T
     *
     * @return Set
     * */
    public <T> Set<T> getSet(Object key, Class<T> tClass) {
        return ArrayUtil.parseSet(this.get(key, Set.class), tClass);
    }

    /**
     * 获取Collection<Map<K, V>>
     *
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Collection<Map<K, V>>
     * */
    public <K, V> Collection<Map<K, V>> getCollectionMap(Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseMap(get(key, Collection.class), kClass, vClass);
    }

    /**
     * 获取List<Map<K, V>>
     *
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return List<Map<K, V>>
     * */
    public <K, V> List<Map<K, V>> getListMap(Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseListMap(get(key, List.class), kClass, vClass);
    }

    /**
     * 获取Set<Map<K, V>>
     *
     * @param key key
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Set<Map<K, V>>
     * */
    public <K, V> Set<Map<K, V>> getSetMap(Object key, Class<K> kClass, Class<V> vClass) {
        return ArrayUtil.parseSetMap(get(key, Set.class), kClass, vClass);
    }

    @Override
    public void build(Object object) {
        try {
            this.data = Json.parse(object, Map.class);
        } catch (Throwable e) {
            try {
                this.data = Json.parse(object, Collection.class);
            } catch (Throwable e1) {
                this.data = object;
            }
        }
    }

    @Override
    public String toString() {
        if (isJson(this.data)) {
            return toString(this.data);
        }
        return ObjectUtil.toString(this.data);
    }

    /**
     * 转成json字符串
     *
     * @param Object Object
     *
     * @return json字符串
     * */
    public static String toString(Object Object) {
        if (Object == null) {
            return null;
        }
        if (Object instanceof String) {
            return ObjectUtil.toString(Object);
        }
        try {
            return JSON.toJSONString(Object, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteEnumsUsingName, JSONWriter.Feature.LargeObject);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * json字符串转成对象
     *
     * @param <T> T
     * @param data data
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T parse(Object data, Class<T> tClass) {
        if (data == null) {
            return  null;
        }
        try {
            return JSON.parseObject(toString(data), tClass);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 是否是json格式
     *
     * @param data data
     *
     * @return 是否是json格式
     * */
    public static boolean isJson(Object data) {
        if (!StringUtil.isEmpty(data)) {
            try {
                Json.parse(data, Map.class);
                return true;
            } catch (Exception e) {
                try {
                    Json.parse(data, List.class);
                    return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    /**
     * 转换
     *
     * @param data data
     *
     * @return Json
     * */
    public static Json parse(Object data) {
        return new Json(data);
    }

}