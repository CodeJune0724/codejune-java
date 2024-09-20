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
public abstract class TaskThreadExecutor<T> {

    private final int threadNum;

    private final ThreadExecutor threadExecutor;

    private final long timeout;

    public TaskThreadExecutor(int threadNum, long timeout) {
        if (threadNum <= 0) {
            throw new BaseException("threadNum <= 0");
        }
        this.threadNum = threadNum;
        this.threadExecutor = null;
        this.timeout = timeout;
    }

    public TaskThreadExecutor(int threadNum) {
        this(threadNum, -1);
    }

    public TaskThreadExecutor(ThreadPoolExecutor threadPoolExecutor, long timeout) {
        if (threadPoolExecutor == null) {
            throw new BaseException("threadPoolExecutor is null");
        }
        this.threadNum = 0;
        this.threadExecutor = new ThreadExecutor(threadPoolExecutor);
        this.timeout = timeout;
    }

    public TaskThreadExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this(threadPoolExecutor, -1);
    }

    /**
     * 执行
     *
     * @param data data
     * */
    public void execute(Collection<T> data) {
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
                threadExecutor.execute(() -> handler(t));
            }
            List<Throwable> await = threadExecutor.await(timeout);
            if (!ObjectUtil.isEmpty(await)) {
                throw new BaseException(await);
            }
        } finally {
            threadExecutor.close();
        }
    }

    /**
     * 数据处理方法
     *
     * @param t 数据
     * */
    protected abstract void handler(T t);

}