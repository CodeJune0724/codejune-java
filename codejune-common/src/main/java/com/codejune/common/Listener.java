package com.codejune.common;

/**
 * 监听器
 *
 * @author ZJ
 * */
public interface Listener<T> {

    /**
     * 监听操作
     *
     * @param data 实时监听的数据
     * */
    void then(T data);

}