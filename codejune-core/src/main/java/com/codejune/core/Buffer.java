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
    public final VALUE get(KEY key) {
        VALUE result = map.get(key);
        if (result == null) {
            if (autoSet()) {
                result = set(key);
            }
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
    public final VALUE set(KEY key) {
        VALUE result = generateValue(key);
        map.put(key, result);
        return result;
    }

    /**
     * 生成值
     *
     * @param key key
     *
     * @return VALUE
     * */
    public abstract VALUE generateValue(KEY key);

    /**
     * 自动设置值
     *
     * @return 是否自动设置值
     * */
    public boolean autoSet() {
        return true;
    }

}