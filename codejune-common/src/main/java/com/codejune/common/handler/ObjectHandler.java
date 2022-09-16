package com.codejune.common.handler;

/**
 * ObjectHandler
 *
 * @author ZJ
 * */
public interface ObjectHandler {

    /**
     * 获取新Object
     *
     * @param key key
     *
     * @return 新key
     * */
    default Object getNewObject(Object key) {
        return key;
    }

}