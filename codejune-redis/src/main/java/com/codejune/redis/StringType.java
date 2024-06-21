package com.codejune.redis;

import com.codejune.core.BaseException;
import com.codejune.core.util.StringUtil;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;

/**
 * StringType
 *
 * @author ZJ
 * */
public final class StringType {

    private final RedisCommands<String, String> redisCommands;

    public StringType(RedisCommands<String, String> redisCommands) {
        this.redisCommands = redisCommands;
    }

    /**
     * set
     *
     * @param key key
     * @param value value
     * @param duration 失效时间
     * */
    public void set(String key, String value, Duration duration) {
        if (StringUtil.isEmpty(key)) {
            throw new BaseException("key is null");
        }
        if (duration == null) {
            this.redisCommands.set(key, value);
        } else {
            this.redisCommands.setex(key, duration.getSeconds(), value);
        }
    }

    /**
     * set
     *
     * @param key key
     * @param value value
     * */
    public void set(String key, String value) {
        this.set(key, value, null);
    }

    /**
     * 只有当key不存在时才设置
     *
     * @param key key
     * @param value value
     * @param duration 失效时间
     *
     * @return 是否设置成功
     * */
    public boolean setOfNoExist(String key, String value, Duration duration) {
        if (StringUtil.isEmpty(key)) {
            throw new BaseException("key is null");
        }
        boolean result = Boolean.TRUE.equals(this.redisCommands.setnx(key, value));
        if (result) {
            if (duration != null) {
                this.redisCommands.expire(key, duration.getSeconds());
            }
        }
        return result;
    }

    /**
     * 只有当key不存在时才设置
     *
     * @param key key
     * @param value value
     *
     * @return 是否设置成功
     * */
    public boolean setOfNoExist(String key, String value) {
        return this.setOfNoExist(key, value, null);
    }

    /**
     * get
     *
     * @param key key
     *
     * @return value
     * */
    public String get(String key) {
        if (StringUtil.isEmpty(key)) {
            throw new BaseException("key is null");
        }
        return this.redisCommands.get(key);
    }

}