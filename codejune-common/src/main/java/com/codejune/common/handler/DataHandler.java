package com.codejune.common.handler;

/**
 * 数据处理
 *
 * @author ZJ
 * */
public interface DataHandler<KEY, VALUE> {

    /**
     * 处理
     *
     * @param key key
     *
     * @return 处理后的数据
     * */
    VALUE handler(KEY key);

}