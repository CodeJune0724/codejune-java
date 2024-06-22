package com.codejune.core;

import com.codejune.core.util.ObjectUtil;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

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
            throw new BaseException("threadNum <= 0");
        }
        this.threadNum = threadNum;
        this.threadExecutor = null;
        this.timeout = timeout;
        execute();
    }

    public FastThreadExecutor(int threadNum) {
        this(threadNum, -1);
    }

    public FastThreadExecutor(ThreadPoolExecutor threadPoolExecutor, long timeout) {
        if (threadPoolExecutor == null) {
            throw new BaseException("threadPoolExecutor is null");
        }
        this.threadNum = 0;
        this.threadExecutor = new ThreadExecutor(threadPoolExecutor);
        this.timeout = timeout;
        execute();
    }

    public FastThreadExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this(threadPoolExecutor, -1);
    }

    /**
     * 获取数据
     *
     * @return Collection
     * */
    public abstract Collection<T> getData();

    /**
     * 数据处理方法
     *
     * @param t 数据
     * */
    public abstract void handler(T t);

    private void execute() {
        Collection<T> data = getData();
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        ThreadExecutor threadExecutor;
        if (this.threadExecutor != null) {
            threadExecutor = new ThreadExecutor(this.threadExecutor.getThreadPoolExecutor());
        } else {
            threadExecutor = new ThreadExecutor(this.threadNum);
        }
        try {
            threadExecutor.startAwait(data.size());
            for (T t : data) {
                threadExecutor.run(() -> handler(t));
            }
            List<Throwable> await = threadExecutor.await(timeout);
            if (!ObjectUtil.isEmpty(await)) {
                throw new BaseException(await);
            }
        } finally {
            threadExecutor.close();
        }
    }

}