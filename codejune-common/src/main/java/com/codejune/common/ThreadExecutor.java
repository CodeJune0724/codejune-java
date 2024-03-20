package com.codejune.common;

import com.codejune.common.util.ThreadUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 多线程执行器
 *
 * @author ZJ
 * */
public final class ThreadExecutor implements Closeable {

    private final ThreadPoolExecutor threadPoolExecutor;

    private final boolean isNew;

    private CountDownLatch countDownLatch = null;

    private final List<Throwable> throwableList = new ArrayList<>();

    public ThreadExecutor(int threadNum) {
        this.threadPoolExecutor = ThreadUtil.getThreadPoolExecutor(threadNum);
        this.isNew = true;
    }

    public ThreadExecutor(ThreadPoolExecutor threadPoolExecutor) {
        if (threadPoolExecutor == null) {
            throw new BaseException("threadPoolExecutor is null");
        }
        this.threadPoolExecutor = threadPoolExecutor;
        this.isNew = false;
    }

    /**
     * 执行
     *
     * @param runnable runnable
     * */
    public void run(Runnable runnable) {
        threadPoolExecutor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                this.throwableList.add(e);
            } finally {
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }
        });
    }


    /**
     * 开启等待
     *
     * @param num 数量
     * */
    public void startAwait(int num) {
        this.countDownLatch = new CountDownLatch(num);
        this.throwableList.clear();
    }

    /**
     * 等待
     *
     * @param timeout 超时时间
     *
     * @return List
     * */
    public List<Throwable> await(long timeout) {
        if (countDownLatch != null) {
            try {
                if (timeout < 0) {
                    countDownLatch.await();
                } else {
                    if (!countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                        throw new BaseException("等待超时");
                    }
                }
            } catch (Exception e) {
                throw new BaseException(e.getMessage());
            }
        }
        return throwableList;
    }

    /**
     * 等待
     *
     * @return List
     * */
    public List<Throwable> await() {
        return await(-1);
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    @Override
    public void close() {
        if (isNew) {
            ThreadUtil.close(this.threadPoolExecutor);
        }
    }

}