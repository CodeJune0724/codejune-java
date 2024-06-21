package com.codejune.redis;

import com.codejune.core.Closeable;
import com.codejune.core.util.StringUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;

/**
 * 锁
 *
 * @author ZJ
 * */
public final class Lock implements Closeable {

    private final RedissonClient redissonClient;

    private final RLock rLock;

    public Lock(Config config, String key) {
        if (config == null) {
            config = new Config();
        }
        org.redisson.config.Config redissonConfig = new org.redisson.config.Config();
        SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
        singleServerConfig.setAddress("redis://" + config.getHost() + ":" + config.getPort());
        if (!StringUtil.isEmpty(config.getPassword())) {
            singleServerConfig.setPassword(config.getPassword());
        }
        singleServerConfig.setDatabase(config.getDatabase());
        this.redissonClient = Redisson.create(redissonConfig);
        this.rLock = this.redissonClient.getLock(key);
    }

    /**
     * 执行
     *
     * @param runnable runnable
     * */
    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        this.rLock.lock();
        try {
            runnable.run();
        } finally {
            this.rLock.unlock();
        }
    }

    @Override
    public void close() {
        try {
            this.redissonClient.shutdown();
        } catch (Throwable ignored) {}
    }

}