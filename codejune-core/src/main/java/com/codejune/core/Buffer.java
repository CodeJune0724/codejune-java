package com.codejune.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存器
 *
 * @author ZJ
 * */
public abstract class Buffer<KEY, VALUE> {

    private final Map<KEY, VALUE> map = new HashMap<>();

    /**
     * 获取
     *
     * @param key key
     *
     * @return value
     * */
    public synchronized final VALUE get(KEY key) {
        VALUE result = map.get(key);
        if (result == null) {
            result = set(key);
            map.put(key, result);
        }
        return result;
    }

    /**
     * set
     *
     * @param key key
     *
     * @return value
     * */
    public abstract VALUE set(KEY key);

}