package com.codejune.common.util;

import java.util.*;
import java.util.function.Function;

/**
 * ArrayUtil
 *
 * @author ZJ
 * */
public final class ArrayUtil {

    /**
     * 指定元素向前移动一位
     *
     * @param <E> E
     * @param list list
     * @param index index
     * */
    public static <E> void prev(List<E> list, int index) {
        if (ObjectUtil.isEmpty(list)) {
            return;
        }
        if (index <= 0 || index >= list.size()) {
            return;
        }
        E current = list.get(index);
        E prev = list.get(index - 1);
        list.set(index - 1, current);
        list.set(index, prev);
    }

    /**
     * 指定元素向后移动一位
     *
     * @param <E> E
     * @param list list
     * @param index index
     * */
    public static <E> void next(List<E> list, int index) {
        if (ObjectUtil.isEmpty(list)) {
            return;
        }
        if (index < 0 || index >= list.size() - 1) {
            return;
        }
        E current = list.get(index);
        E next = list.get(index + 1);
        list.set(index, next);
        list.set(index + 1, current);
    }

    /**
     * 将指定元素移动到指定位置
     *
     * @param <E> E
     * @param list list
     * @param index 指定元素
     * @param toIndex 要移动的位置
     * */
    public static <E> void move(List<E> list, int index, int toIndex) {
        if (ObjectUtil.isEmpty(list)) {
            return;
        }
        if (index == toIndex) {
            return;
        }
        if (index >= 0 && index < list.size()) {
            if (toIndex >= 0 && toIndex < list.size()) {
                if (toIndex > index) {
                    for (int i = 0; i < toIndex - index; i++) {
                        next(list, index + i);
                    }
                }
                if (index > toIndex) {
                    for (int i = 0; i < index - toIndex; i++) {
                        prev(list, index - i);
                    }
                }
            }
        }
    }

    /**
     * 转换
     *
     * @param collection collection
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return Collection
     * */
    @SuppressWarnings("unchecked")
    public static <PARAM, RETURN> Collection<RETURN> parse(Collection<PARAM> collection, Function<PARAM, RETURN> action) {
        if (collection == null) {
            return null;
        }
        if (action == null) {
            action = param -> null;
        }
        Collection<RETURN> result;
        try {
            result = (Collection<RETURN>) ObjectUtil.newInstance(collection.getClass());
        } catch (Exception e) {
            result = new ArrayList<>();
        }
        for (PARAM item : collection) {
            RETURN then = action.apply(item);
            if (then == null) {
                continue;
            }
            result.add(then);
        }
        return result;
    }

    /**
     * 转换
     *
     * @param collection collection
     * @param tClass tClass
     * @param <T> T
     *
     * @return Collection
     * */
    public static <T> Collection<T> parse(Collection<?> collection, Class<T> tClass) {
        return parse(collection, o -> ObjectUtil.transform(o, tClass));
    }

    /**
     * 转换成list
     *
     * @param collection collection
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return list
     * */
    @SuppressWarnings("unchecked")
    public static <PARAM, RETURN> List<RETURN> parseList(Collection<PARAM> collection, Function<PARAM, RETURN> action) {
        if (collection == null) {
            return null;
        }
        if (action == null) {
            action = param -> null;
        }
        List<RETURN> result;
        if (collection instanceof List<?>) {
            try {
                result = (List<RETURN>) ObjectUtil.newInstance(collection.getClass());
            } catch (Exception e) {
                result = new ArrayList<>();
            }
        } else {
            result = new ArrayList<>();
        }
        for (PARAM item : collection) {
            RETURN then = action.apply(item);
            if (then == null) {
                continue;
            }
            result.add(then);
        }
        return result;
    }

    /**
     * 转换成list
     *
     * @param collection collection
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> List<T> parseList(Collection<?> collection, Class<T> tClass) {
        return parseList(collection, o -> ObjectUtil.transform(o, tClass));
    }

    /**
     * 转换成set
     *
     * @param collection collection
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return Set
     * */
    @SuppressWarnings("unchecked")
    public static <PARAM, RETURN> Set<RETURN> parseSet(Collection<PARAM> collection, Function<PARAM, RETURN> action) {
        if (collection == null) {
            return null;
        }
        if (action == null) {
            action = param -> null;
        }
        Set<RETURN> result;
        if (collection instanceof Set<?>) {
            try {
                result = (Set<RETURN>) ObjectUtil.newInstance(collection.getClass());
            } catch (Exception e) {
                result = new HashSet<>();
            }
        } else {
            result = new HashSet<>();
        }
        for (PARAM item : collection) {
            RETURN then = action.apply(item);
            if (then == null) {
                continue;
            }
            result.add(then);
        }
        return result;
    }

    /**
     * 转换成set
     *
     * @param collection collection
     * @param tClass tClass
     * @param <T> T
     *
     * @return Set
     * */
    public static <T> Set<T> parseSet(Collection<?> collection, Class<T> tClass) {
        return parseSet(collection, o -> ObjectUtil.transform(o, tClass));
    }

    /**
     * 转换成Collection<Map<K, V>>
     *
     * @param collection collection
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Collection<Map<K, V>>
     * */
    public static <K, V> Collection<Map<K, V>> parseMap(Collection<?> collection, Class<K> kClass, Class<V> vClass) {
        return parse(collection, o -> MapUtil.parse(o, kClass, vClass));
    }

    /**
     * 转换成List<Map<K, V>>
     *
     * @param collection collection
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return List<Map<K, V>>
     * */
    public static <K, V> List<Map<K, V>> parseListMap(Collection<?> collection, Class<K> kClass, Class<V> vClass) {
        return parseList(collection, o -> MapUtil.parse(o, kClass, vClass));
    }

    /**
     * 转换成Set<Map<K, V>>
     *
     * @param collection collection
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Set<Map<K, V>>
     * */
    public static <K, V> Set<Map<K, V>> parseSetMap(Collection<?> collection, Class<K> kClass, Class<V> vClass) {
        return parseSet(collection, o -> MapUtil.parse(o, kClass, vClass));
    }

    /**
     * 转换成list
     *
     * @param ts ts
     * @param <T> 泛型
     *
     * @return List
     * */
    @SafeVarargs
    public static <T> List<T> asList(T... ts) {
        if (ts == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(ts));
    }

    /**
     * list转成string
     *
     * @param collection collection
     * @param action action
     * @param split 分隔符
     * @param <T> 航行
     * */
    public static <T> String toString(Collection<T> collection, Function<T, String> action, String split) {
        if (ObjectUtil.isEmpty(collection)) {
            return null;
        }
        if (action == null) {
            action = t -> null;
        }
        if (split == null) {
            split = "";
        }
        String result = "";
        for (T t : collection) {
            String string = action.apply(t);
            if (string == null) {
                continue;
            }
            result = StringUtil.append(result, string, split);
        }
        return ObjectUtil.toString(ObjectUtil.subString(result, result.length() - split.length()));
    }

    /**
     * 生成序列
     *
     * @param size 大小
     * @param startIndex 开始索引
     *
     * @return 序列
     * */
    public static List<Integer> createSequence(int size, int startIndex) {
        List<Integer> result = new ArrayList<>();
        if (size <= 0) {
            return result;
        }
        for (int i = startIndex; i < size; i++) {
            result.add(i);
        }
        return result;
    }

    /**
     * 生成序列
     *
     * @param size 大小
     *
     * @return 序列
     * */
    public static List<Integer> createSequence(int size) {
        return createSequence(size, 0);
    }

    /**
     * 截取
     *
     * @param tList tList
     * @param startIndex 开始位置
     * @param length 截取长度
     *
     * @return List<T>
     * */
    public static <T> List<T> split(List<T> tList, int startIndex, int length) {
        if (tList == null) {
            return null;
        }
        int size = tList.size();
        if (startIndex > size) {
            return new ArrayList<>();
        }
        int endIndex = startIndex + length;
        if (endIndex > size) {
            endIndex = size;
        }
        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }
        return tList.subList(startIndex, endIndex);
    }

    /**
     * 获取
     *
     * @param list list
     * @param index index
     * @param tClass tClass
     * @param <T> T
     *
     * @return T
     * */
    public static <T> T get(List<?> list, int index, Class<T> tClass) {
        if (ObjectUtil.isEmpty(list) || index >= list.size()) {
            return null;
        }
        return ObjectUtil.transform(list.get(index), tClass);
    }

    /**
     * 获取
     *
     * @param list list
     * @param index index
     * @param <T> T
     *
     * @return T
     * */
    public static <T> T get(List<T> list, int index) {
        if (ObjectUtil.isEmpty(list) || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 获取map
     *
     * @param list list
     * @param index index
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Map
     * */
    public static <K, V> Map<K, V> getMap(List<?> list, int index, Class<K> kClass, Class<V> vClass) {
        return MapUtil.parse(get(list, index, Map.class), kClass, vClass);
    }

    /**
     * 获取collection
     *
     * @param list list
     * @param index index
     * @param vClass vClass
     * @param <V> V
     *
     * @return Collection
     * */
    public static <V> Collection<V> getCollection(List<?> list, int index, Class<V> vClass) {
        return parse(get(list, index, Collection.class), vClass);
    }

    /**
     * 获取Collection<Map<K, V>>
     *
     * @param list list
     * @param index index
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Collection<Map<K, V>>
     * */
    public static <K, V> Collection<Map<K, V>> getCollectionMap(List<?> list, int index, Class<K> kClass, Class<V> vClass) {
        return parseMap(get(list, index, Collection.class), kClass, vClass);
    }

    /**
     * 获取list
     *
     * @param list list
     * @param index index
     * @param vClass vClass
     * @param <V> V
     *
     * @return List
     * */
    public static <V> List<V> getList(List<?> list, int index, Class<V> vClass) {
        return parseList(get(list, index, Collection.class), vClass);
    }

    /**
     * 获取List<Map<K, V>>
     *
     * @param list list
     * @param index index
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return List<Map<K, V>>
     * */
    public static <K, V> List<Map<K, V>> getListMap(List<?> list, int index, Class<K> kClass, Class<V> vClass) {
        return parseListMap(get(list, index, Collection.class), kClass, vClass);
    }

    /**
     * 获取set
     *
     * @param list list
     * @param index index
     * @param vClass vClass
     * @param <V> V
     *
     * @return Set
     * */
    public static <V> Set<V> getSet(List<?> list, int index, Class<V> vClass) {
        return parseSet(get(list, index, Collection.class), vClass);
    }

    /**
     * 获取Set<Map<K, V>>
     *
     * @param list list
     * @param index index
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Set<Map<K, V>>
     * */
    public static <K, V> Set<Map<K, V>> getSetMap(List<?> list, int index, Class<K> kClass, Class<V> vClass) {
        return parseSetMap(get(list, index, Collection.class), kClass, vClass);
    }

}