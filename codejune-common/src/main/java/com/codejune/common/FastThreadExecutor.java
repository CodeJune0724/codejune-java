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

    private final long timeout;

    public FastThreadExecutor(int threadNum, long timeout) {
        if (threadNum <= 0) {
            throw new InfoException("threadNum <= 0");
        }
        this.threadNum = threadNum;
        this.timeout = timeout;
    }

    public FastThreadExecutor(int threadNum) {
        this(threadNum, -1);
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
        ThreadExecutor threadExecutor = new ThreadExecutor(threadNum);
        threadExecutor.startAwait(collection.size());
        for (T t : collection) {
            threadExecutor.execute(() -> handler(t));
        }
        List<Throwable> await = threadExecutor.await(timeout);
        if (!ObjectUtil.isEmpty(await)) {
            throw new InfoException(await);
        }
    }

}