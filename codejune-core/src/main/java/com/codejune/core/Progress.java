package com.codejune.core;

import com.codejune.core.util.ObjectUtil;

/**
 * 进度
 *
 * @author ZJ
 * */
public abstract class Progress {

    private long current = 0;

    private long lastCurrent = 0;

    private long time = System.currentTimeMillis();

    private long speed = 0;

    private final long count;

    public Progress(long count) {
        if (count < 0) {
            throw new BaseException("size is < 0");
        }
        this.count = count;
    }

    /**
     * 监听
     *
     * @param progress progress
     * */
    public abstract void listen(Progress progress);

    /**
     * getCount
     *
     * @return count
     * */
    public final long getCount() {
        return this.count;
    }

    /**
     * getCurrent
     *
     * @return current
     * */
    public final long getCurrent() {
        return this.current;
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
            if (this.current >= this.count) {
                return;
            }
            this.current = this.current + size;
            if (this.current > this.count) {
                this.current = this.count;
            }
            if (System.currentTimeMillis() - this.time > 1000) {
                this.speed = this.current - this.lastCurrent;
                this.lastCurrent = this.current;
                this.time = System.currentTimeMillis();
            }
            this.listen(this);
        }
    }

    /**
     * 推进进度
     * */
    public final void countDown() {
        this.countDown(1);
    }

    /**
     * 转换成百分比
     *
     * @return 百分比
     * */
    public final Double getPercentage() {
        Double currentSizeDouble = ObjectUtil.parse(this.current, Double.class);
        Double totalSizeDouble = ObjectUtil.parse(this.count, Double.class);
        if (totalSizeDouble == 0) {
            return totalSizeDouble;
        }
        return ObjectUtil.parse(String.format("%.2f", (currentSizeDouble / totalSizeDouble) * 100), Double.class);
    }

    /**
     * 获取下载速度
     *
     * @return 下载速度
     * */
    public final long getSpeed() {
        return this.speed;
    }

    /**
     * 获取下载速度
     *
     * @return 下载速度
     * */
    public final String getSpeedString() {
        if (this.speed < 1024) {
            return this.speed + "B/s";
        }
        else if (this.speed < 1024 * 1024) {
            return (this.speed / 1024) + "KB/s";
        } else {
            return new Calculator(this.speed).divide(1024, 1).divide(1024, 1).count() + "MB/s";
        }
    }

}