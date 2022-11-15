package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import java.util.*;

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
        if (list == null) {
            return;
        }
        if (list.size() == 0) {
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
        if (list == null) {
            return;
        }
        if (list.size() == 0) {
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
        if (list == null) {
            return;
        }
        if (list.size() == 0) {
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
     * 转换泛型
     *
     * @param list list
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> List<T> parse(List<?> list, Class<T> tClass) {
        if (list == null) {
            return null;
        }
        if (tClass == null) {
            throw new InfoException("tClass is null");
        }
        List<T> result = new ArrayList<>();
        for (Object item : list) {
            result.add(ObjectUtil.transform(item, tClass));
        }
        return result;
    }

    /**
     * 转换泛型
     *
     * @param object object
     * @param tClass tClass
     * @param <T> T
     *
     * @return List
     * */
    public static <T> List<T> parse(Object object, Class<T> tClass) {
        return parse(ObjectUtil.transform(object, List.class), tClass);
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
    public static <T> List<T> parse(T... ts) {
        if (ts == null) {
            return null;
        }
        return Arrays.asList(ts);
    }

    /**
     * list转成string
     *
     * @param collection collection
     * @param stringHandler 转换方法
     * @param split 分隔符
     * @param <T> 航行
     * */
    public static <T> String toString(Collection<T> collection, StringHandler<T> stringHandler, String split) {
        if (ObjectUtil.isEmpty(collection)) {
            return null;
        }
        if (stringHandler == null) {
            stringHandler = ObjectUtil::toString;
        }
        if (split == null) {
            split = "";
        }
        String result = "";
        for (T t : collection) {
            result = StringUtil.append(result, stringHandler.toString(t), split);
        }
        return result.substring(0, result.length() - split.length());
    }

    /**
     * 生成序列
     *
     * @param size 大小
     *
     * @return 序列
     * */
    public static List<Integer> createSequence(int size) {
        List<Integer> result = new ArrayList<>();
        if (size <= 0) {
            return result;
        }
        for (int i = 0; i < size; i++) {
            result.add(i);
        }
        return result;
    }

    public interface StringHandler<T> {
        String toString(T t);

    }

}