package com.codejune.common;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.ThreadUtil;

/**
 * 进度
 *
 * @author ZJ
 * */
public abstract class Progress implements Listener<Progress> {

    private long current = 0;

    private final long total;

    public Progress(long total, int listenInterval) {
        if (total < 0) {
            throw new InfoException("size is < 0");
        }
        this.total = total;
        Thread thread = new Thread(() -> {
            while (true) {
                then(Progress.this);
                ThreadUtil.sleep(listenInterval);
                if (current >= total) {
                    then(Progress.this);
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

    public long getTotal() {
        return total;
    }

    public long getCurrent() {
        return current;
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
            this.current = this.current + size;
            if (this.current > this.total) {
                this.current = this.total;
            }
        }
    }

    /**
     * 推进进度
     * */
    public final void countDown() {
        countDown(1);
    }

    /**
     * 转换成百分比
     *
     * @return 百分比
     * */
    public final Double getPercentage() {
        Double currentSizeDouble = ObjectUtil.transform(current, Double.class);
        Double totalSizeDouble = ObjectUtil.transform(total, Double.class);
        if (totalSizeDouble == 0) {
            return totalSizeDouble;
        }
        return ObjectUtil.transform(String.format("%.2f", (currentSizeDouble / totalSizeDouble) * 100), Double.class);
    }

}