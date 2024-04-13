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
     * 数据转换
     *
     * @param list list
     * @param action action
     * @param <PARAM> 参数类型
     * @param <RETURN> 返回类型
     *
     * @return list
     * */
    public static <PARAM, RETURN> List<RETURN> parse(List<PARAM> list, Function<PARAM, RETURN> action) {
        if (list == null) {
            return null;
        }
        if (action == null) {
            action = param -> null;
        }
        List<RETURN> result = new ArrayList<>();
        for (PARAM item : list) {
            RETURN then = action.apply(item);
            if (then == null) {
                continue;
            }
            result.add(then);
        }
        return result;
    }

    /**
     * 转换泛型
     *
     * @param list list
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> List<T> parse(List<?> list, Class<T> tClass) {
        return parse(list, o -> ObjectUtil.transform(o, tClass));
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
        return Arrays.asList(ts);
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
     * @param collection collection
     * @param index index
     * @param tClass tClass
     * @param <T> T
     *
     * @return T
     * */
    public static <T> T get(List<?> collection, int index, Class<T> tClass) {
        if (ObjectUtil.isEmpty(collection) || index >= collection.size()) {
            return null;
        }
        return ObjectUtil.transform(collection.get(index), tClass);
    }

    /**
     * 获取
     *
     * @param collection collection
     * @param index index
     * @param <T> T
     *
     * @return T
     * */
    public static <T> T get(List<T> collection, int index) {
        if (ObjectUtil.isEmpty(collection) || index >= collection.size()) {
            return null;
        }
        return collection.get(index);
    }

}