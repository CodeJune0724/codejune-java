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
        ThreadExecutor threadExecutor;
        if (this.threadExecutor != null) {
            threadExecutor = new ThreadExecutor(this.threadExecutor.getThreadPoolExecutor());
        } else {
            threadExecutor = new ThreadExecutor(this.threadNum);
        }
        try {
            threadExecutor.startAwait(collection.size());
            for (T t : collection) {
                threadExecutor.run(() -> handler(t));
            }
            List<Throwable> await = threadExecutor.await(timeout);
            if (!ObjectUtil.isEmpty(await)) {
                throw new InfoException(await);
            }
        } finally {
            threadExecutor.close();
        }
    }

}