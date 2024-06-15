package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.pool.Config;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.time.Duration;

/**
 * 池子
 *
 * @author ZJ
 * */
public abstract class Pool<T> implements Closeable {

    private final GenericObjectPool<T> genericObjectPool;

    public Pool(Config config) {
        if (config == null) {
            config = new Config();
        }
        BasePooledObjectFactory<T> basePooledObjectFactory = new BasePooledObjectFactory<>() {
            @Override
            public T create() {
                return Pool.this.create();
            }
            @Override
            public PooledObject<T> wrap(T obj) {
                return new DefaultPooledObject<>(obj);
            }
            @Override
            public boolean validateObject(PooledObject<T> pooledObject) {
                T object = pooledObject.getObject();
                boolean result = false;
                try {
                    result = Pool.this.check(object);
                } catch (Throwable ignored) {}
                if (object instanceof Closeable closeable) {
                    Closeable.closeNoError(closeable);
                }
                return result;
            }
            @Override
            public void destroyObject(PooledObject<T> pooledObject) {
                T object = pooledObject.getObject();
                if (object instanceof Closeable closeable) {
                    Closeable.closeNoError(closeable);
                }
                try {
                    super.destroyObject(pooledObject);
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }
        };
        GenericObjectPoolConfig<T> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(config.getSize());
        genericObjectPoolConfig.setMaxIdle(config.getMaxIdle() < 0 ? config.getSize() : config.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(Math.max(config.getMinIdle(), 0));
        genericObjectPoolConfig.setMaxWait(config.getMaxWait());
        Duration whileCheckTime = config.getWhileCheckTime();
        genericObjectPoolConfig.setTestWhileIdle(whileCheckTime != null && whileCheckTime.toMillis() > 0);
        genericObjectPoolConfig.setTimeBetweenEvictionRuns(whileCheckTime);
        this.genericObjectPool = new GenericObjectPool<>(basePooledObjectFactory, genericObjectPoolConfig);
    }

    public Pool(int size) {
        this(new Config().setSize(size));
    }

    /**
     * 创建对象
     *
     * @return T
     * */
    public abstract T create();

    /**
     * 对象校验方法
     *
     * @param t 获取到的对象
     *
     * @return 对象是否有效
     * */
    public boolean check(T t) {
        return true;
    }

    /**
     * 获取
     *
     * @return T
     * */
    public final T get() {
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 收回
     *
     * @param t t
     * */
    public final void returnObject(T t) {
        if (t == null) {
            return;
        }
        genericObjectPool.returnObject(t);
    }

    /**
     * 获取大小
     *
     * @return 大小
     * */
    public final int getSize() {
        return this.genericObjectPool.getMaxTotal();
    }

    /**
     * 获取活跃大小
     *
     * @return 活跃的数量
     * */
    public final int getActiveNumber() {
        return this.genericObjectPool.getNumActive();
    }

    /**
     * 获取空闲的数量
     *
     * @return 空闲的数量
     * */
    public final int getIdleNumber() {
        return this.genericObjectPool.getNumIdle();
    }

    @Override
    public void close() {
        this.genericObjectPool.close();
    }

}