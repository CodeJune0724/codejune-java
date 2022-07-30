package com.codejune.common.handler;

/**
 * KeyHandler
 *
 * @author ZJ
 * */
public interface KeyHandler {

    /**
     * 获取新key
     *
     * @param key key
     *
     * @return 新key
     * */
    default Object getNewKey(Object key) {
        return key;
    }

}