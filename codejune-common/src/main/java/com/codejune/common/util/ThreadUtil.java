package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
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
            throw new InfoException(e.getMessage());
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
        return new ThreadPoolExecutor(num, num, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
    }

}