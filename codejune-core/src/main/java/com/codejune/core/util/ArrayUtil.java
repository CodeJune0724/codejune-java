package com.codejune.core.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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
     * 过滤
     *
     * @param collection collection
     * @param predicate predicate
     * @param <COLLECTION> COLLECTION
     * @param <T> T
     *
     * @return Collection
     * */
    @SuppressWarnings("unchecked")
    public static <COLLECTION extends Collection<T>, T> COLLECTION filter(COLLECTION collection, Predicate<T> predicate) {
        if (collection == null || predicate == null) {
            return collection;
        }
        Collection<T> result;
        try {
            result = (Collection<T>) ObjectUtil.newInstance(collection.getClass());
        } catch (Exception e) {
            result = new ArrayList<>();
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return (COLLECTION) result;
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
            result.add(action.apply(item));
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
        return parse(collection, o -> ObjectUtil.parse(o, tClass));
    }

    /**
     * 转换成List
     *
     * @param list list
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return List
     * */
    public static <PARAM, RETURN> List<RETURN> parseList(List<PARAM> list, Function<PARAM, RETURN> action) {
        return (List<RETURN>) parse(list, action);
    }

    /**
     * 转换成List
     *
     * @param list list
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> List<T> parseList(List<?> list, Class<T> tClass) {
        return parseList(list, o -> ObjectUtil.parse(o, tClass));
    }

    /**
     * 转换成Set
     *
     * @param set set
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return Collection
     * */
    public static <PARAM, RETURN> Set<RETURN> parseSet(Set<PARAM> set, Function<PARAM, RETURN> action) {
        return (Set<RETURN>) parse(set, action);
    }

    /**
     * 转换成Set
     *
     * @param set set
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> Set<T> parseSet(Set<?> set, Class<T> tClass) {
        return parseSet(set, o -> ObjectUtil.parse(o, tClass));
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
     * @param list list
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return List<Map<K, V>>
     * */
    public static <K, V> List<Map<K, V>> parseListMap(List<?> list, Class<K> kClass, Class<V> vClass) {
        return parseList(list, o -> MapUtil.parse(o, kClass, vClass));
    }

    /**
     * 转换成Set<Map<K, V>>
     *
     * @param set set
     * @param kClass kClass
     * @param vClass vClass
     * @param <K> K
     * @param <V> V
     *
     * @return Set<Map<K, V>>
     * */
    public static <K, V> Set<Map<K, V>> parseSetMap(Set<?> set, Class<K> kClass, Class<V> vClass) {
        return parseSet(set, o -> MapUtil.parse(o, kClass, vClass));
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
        return ObjectUtil.parse(list.get(index), tClass);
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

}