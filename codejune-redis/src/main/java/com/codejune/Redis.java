package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.StringUtil;
import com.codejune.redis.Config;
import com.codejune.redis.StringType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.time.Duration;

/**
 * redis
 *
 * @author ZJ
 * */
public final class Redis implements Closeable {

    private final RedisClient redisClient;

    private final StatefulRedisConnection<String, String> stringStringStatefulRedisConnection;

    public Redis(Config config) {
        if (config == null) {
            config = new Config();
        }
        if (StringUtil.isEmpty(config.getPassword())) {
            config.setPassword("");
        }
        this.redisClient = RedisClient.create("redis://" + config.getPassword() + "@" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());
        this.stringStringStatefulRedisConnection = this.redisClient.connect();
    }

    public Redis(String host, int port, int database) {
        this(new Config().setHost(host).setPort(port).setDatabase(database));
    }

    /**
     * 操作string
     *
     * @return String
     * */
    public StringType string() {
        return new StringType(this.stringStringStatefulRedisConnection.sync());
    }

    /**
     * 设置key的过期时间
     *
     * @param key key
     * @param duration duration
     * */
    public void expire(String key, Duration duration) {
        if (StringUtil.isEmpty(key)) {
            throw new BaseException("key is null");
        }
        if (duration == null) {
            throw new BaseException("duration is null");
        }
        this.stringStringStatefulRedisConnection.sync().expire(key, duration.getSeconds());
    }

    @Override
    public void close() {
        try {
            stringStringStatefulRedisConnection.close();
        } catch (Throwable ignored) {}
        try {
            redisClient.shutdown();
        } catch (Throwable ignored) {}
    }

}