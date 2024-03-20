package com.codejune.common;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.io.Closeable;
import java.time.Duration;

/**
 * 池子
 *
 * @author ZJ
 * */
public abstract class Pool<T> implements Closeable {

    private final GenericObjectPool<Source<T>> genericObjectPool;

    private final int size;

    public Pool(int size) {
        Pool<T> pool = this;
        BasePooledObjectFactory<Source<T>> basePooledObjectFactory = new BasePooledObjectFactory<>() {
            @Override
            public Source<T> create() {
                return new Source<>(pool.create());
            }
            @Override
            public PooledObject<Source<T>> wrap(Source<T> obj) {
                return new DefaultPooledObject<>(obj);
            }
        };
        GenericObjectPoolConfig<Source<T>> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
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
     * @return Source<T>
     * */
    public final Source<T> get() {
        Source<T> source = null;
        try {
            source = genericObjectPool.borrowObject();
            T result = source.getSource();
            if (!check(result)) {
                if (result instanceof Closeable closeable) {
                    try {
                        closeable.close();
                    } catch (Throwable ignored) {}
                }
                result = create();
            }
            source.setSource(result);
            return source;
        } catch (Throwable e) {
            returnObject(source);
            throw new BaseException(e);
        }
    }

    /**
     * 收回
     *
     * @param source source
     * */
    public final void returnObject(Source<T> source) {
        if (source == null) {
            return;
        }
        genericObjectPool.returnObject(source);
    }

    /**
     * 获取大小
     *
     * @return 大小
     * */
    public final int getSize() {
        return this.size;
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
        for (int i = 0; i < size; i++) {
            if (this.get().getSource() instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (Exception ignored) {}
            }
        }
        this.genericObjectPool.clear();
        this.genericObjectPool.close();
    }

    public static final class Source<T> {

        private T source;

        private Source(T source) {
            this.source = source;
        }

        public T getSource() {
            return source;
        }

        public void setSource(T source) {
            this.source = source;
        }

    }

}