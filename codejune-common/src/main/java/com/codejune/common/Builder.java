package com.codejune.common;

/**
 * 生成器
 *
 * @author ZJ
 * */
public interface Builder<T> {

    /**
     * 生成
     *
     * @param object 参数
     *
     * @return 新对象
     * */
    T build(Object object);

}