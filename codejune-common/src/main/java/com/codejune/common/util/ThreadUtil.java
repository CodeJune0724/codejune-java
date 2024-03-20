package com.codejune.common.util;

import com.codejune.common.BaseException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadUtil
 *
 * @author ZJ
 * */
public final class ThreadUtil {

    /**
     * 线程休眠
     *
     * @param millis 休眠的毫秒数
     * */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 获取一个线程池
     *
     * @param num 线程数量
     *
     * @return 线程池
     * */
    public static ThreadPoolExecutor getThreadPoolExecutor(int num) {
        if (num <= 0) {
            throw new BaseException("线程数 <= 0");
        }
        return new ThreadPoolExecutor(num, num, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
    }

    /**
     * 关闭线程池
     *
     * @param threadPoolExecutor threadPoolExecutor
     * */
    public static void close(ThreadPoolExecutor threadPoolExecutor) {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

}