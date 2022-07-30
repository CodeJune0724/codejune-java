package com.codejune.common;

/**
 * ModelAble
 *
 * @author ZJ
 * */
public interface ModelAble<T> {

    /**
     * 给对象赋值
     *
     * @param object 参数
     *
     * @return this
     * */
    T assignment(Object object);

}