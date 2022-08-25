package com.codejune.common;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import java.util.Collection;
import java.util.List;

/**
 * 快速多线程处理
 *
 * @param <T> 数据泛型
 *
 * @author ZJ
 * */
public abstract class FastThreadExecutor<T> {

    private final int threadNum;

    private final ThreadExecutor threadExecutor;

    private final long timeout;

    public FastThreadExecutor(int threadNum, long timeout) {
        if (threadNum <= 0) {
            throw new InfoException("threadNum <= 0");
        }
        this.threadNum = threadNum;
        this.threadExecutor = null;
        this.timeout = timeout;
    }

    public FastThreadExecutor(int threadNum) {
        this(threadNum, -1);
    }

    public FastThreadExecutor(ThreadExecutor threadExecutor, long timeout) {
        if (threadExecutor == null) {
            throw new InfoException("threadExecutor is null");
        }
        this.threadNum = 0;
        this.threadExecutor = threadExecutor;
        this.timeout = timeout;
    }

    public FastThreadExecutor(ThreadExecutor threadExecutor) {
        this(threadExecutor, -1);
    }

    /**
     * 数据处理方法
     *
     * @param t 数据
     * */
    public abstract void handler(T t);

    /**
     * 开始执行
     *
     * @param collection 数据
     * */
    public final void run(Collection<T> collection) {
        if (ObjectUtil.isEmpty(collection)) {
            return;
        }
        ThreadExecutor threadExecutor = getThreadExecutor();
        try {
            threadExecutor.startAwait(collection.size());
            for (T t : collection) {
                threadExecutor.execute(() -> {
                    handler(t);
                });
            }
            List<Throwable> await = threadExecutor.await(timeout);
            if (!ObjectUtil.isEmpty(await)) {
                throw new InfoException(await);
            }
        } finally {
            if (isClose()) {
                threadExecutor.close();
            }
        }
    }

    /**
     * 开始执行
     *
     * @param num 要遍历的次数
     * */
    public final void run(T num) {
        if (num == null) {
            return;
        }
        if (!(num instanceof Number)) {
            return;
        }
        Integer integer = ObjectUtil.transform(num, Integer.class);
        ThreadExecutor threadExecutor = getThreadExecutor();
        try {
            threadExecutor.startAwait(integer);
            for (int i = 0; i < integer; i++) {
                threadExecutor.execute(() -> {
                    handler(null);
                });
            }
            List<Throwable> await = threadExecutor.await(timeout);
            if (!ObjectUtil.isEmpty(await)) {
                throw new InfoException(await);
            }
        } finally {
            if (isClose()) {
                threadExecutor.close();
            }
        }
    }

    private ThreadExecutor getThreadExecutor() {
        if (this.threadExecutor == null) {
            return new ThreadExecutor(this.threadNum);
        }
        return this.threadExecutor;
    }

    private boolean isClose() {
        return this.threadExecutor == null;
    }

}