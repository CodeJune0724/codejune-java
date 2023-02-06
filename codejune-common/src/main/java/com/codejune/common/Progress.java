package com.codejune.common;

import com.codejune.common.exception.InfoException;
import com.codejune.common.listener.ProgressListener;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.ThreadUtil;

/**
 * 进度
 *
 * @author ZJ
 * */
public abstract class Progress implements ProgressListener {

    private long currentSize = 0;

    private final long totalSize;

    public Progress(long totalSize, int listenInterval) {
        if (totalSize < 0) {
            throw new InfoException("size is < 0");
        }
        this.totalSize = totalSize;
        Thread thread = new Thread(() -> {
            while (true) {
                listen(Progress.this);
                ThreadUtil.sleep(listenInterval);
                if (currentSize >= totalSize) {
                    listen(Progress.this);
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Progress(long size) {
        this(size, 1000);
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    /**
     * 推进进度
     *
     * @param size 大小
     * */
    public final void countDown(long size) {
        synchronized (this) {
            if (size < 0) {
                return;
            }
            this.currentSize = this.currentSize + size;
            if (this.currentSize > this.totalSize) {
                this.currentSize = this.totalSize;
            }
        }
    }

    /**
     * 转换成百分比
     *
     * @return 百分比
     * */
    public final Double getPercentage() {
        Double currentSizeDouble = ObjectUtil.transform(currentSize, Double.class);
        Double totalSizeDouble = ObjectUtil.transform(totalSize, Double.class);
        if (totalSizeDouble == 0) {
            return totalSizeDouble;
        }
        return ObjectUtil.transform(String.format("%.2f", (currentSizeDouble / totalSizeDouble) * 100), Double.class);
    }

}