package com.codejune.common;

import com.codejune.common.exception.InfoException;
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

    private final int size;

    public Pool(int size) {
        Pool<T> pool = this;
        BasePooledObjectFactory<T> basePooledObjectFactory = new BasePooledObjectFactory<T>() {
            @Override
            public T create() {
                return pool.create();
            }

            @Override
            public PooledObject<T> wrap(T t) {
                return new DefaultPooledObject<>(t);
            }
        };
        GenericObjectPoolConfig<T> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(size);
        genericObjectPoolConfig.setMaxIdle(size);
        genericObjectPoolConfig.setMaxWait(Duration.ofMinutes(1));
        this.genericObjectPool = new GenericObjectPool<>(basePooledObjectFactory, genericObjectPoolConfig);
        this.size = size;
    }

    /**
     * 创建对象
     *
     * @return T
     * */
    public abstract T create();

    /**
     * 获取
     *
     * @return T
     * */
    public final T get() {
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 收回
     *
     * @param t t
     * */
    public final void returnObject(T t) {
        genericObjectPool.returnObject(t);
    }

    /**
     * 获取大小
     *
     * @return 大小
     * */
    public final int getSize() {
        return this.size;
    }

    @Override
    public void close() {
        for (int i = 0; i < size; i++) {
            T t = this.get();
            if (t instanceof Closeable) {
                ((Closeable) t).close();
            }
        }
        this.genericObjectPool.clear();
        this.genericObjectPool.close();
    }

}