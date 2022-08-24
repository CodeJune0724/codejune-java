package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import java.util.ArrayList;
import java.util.List;

/**
 * ListUtil
 *
 * @author ZJ
 * */
public final class ListUtil {

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
    public static <T> List<T> parseToGeneric(List<?> list, Class<T> tClass) {
        if (list == null) {
            return null;
        }
        if (tClass == null) {
            throw new InfoException("tClass is null");
        }
        List<T> result = new ArrayList<>();
        for (Object item : list) {
            result.add(ObjectUtil.parse(item, tClass));
        }
        return result;
    }

    /**
     * list转成string
     *
     * @param tList tList
     * @param stringHandler 转换方法
     * @param split 分隔符
     * @param <T> 航行
     * */
    public static <T> String toString(List<T> tList, StringHandler<T> stringHandler, String split) {
        if (tList == null) {
            return null;
        }
        if (stringHandler == null) {
            stringHandler = ObjectUtil::toString;
        }
        if (split == null) {
            split = "";
        }
        String result = "";
        for (T t : tList) {
            result = StringUtil.append(result, stringHandler.toString(t), split);
        }
        return result.substring(0, result.length() - split.length());
    }

    public interface StringHandler<T> {
        String toString(T t);

    }

}