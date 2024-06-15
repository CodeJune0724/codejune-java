package com.codejune.pool;

import java.time.Duration;

/**
 * 连接池配置
 *
 * @author ZJ
 * */
public final class Config {

    private int size;

    private int MaxIdle = -1;

    private int MinIdle = 0;

    private Duration MaxWait = Duration.ofMinutes(1);

    private Duration whileCheckTime;

    public int getSize() {
        return size;
    }

    public Config setSize(int size) {
        this.size = size;
        return this;
    }

    public int getMaxIdle() {
        return MaxIdle;
    }

    public Config setMaxIdle(int maxIdle) {
        MaxIdle = maxIdle;
        return this;
    }

    public int getMinIdle() {
        return MinIdle;
    }

    public Config setMinIdle(int minIdle) {
        MinIdle = minIdle;
        return this;
    }

    public Duration getMaxWait() {
        return MaxWait;
    }

    public Config setMaxWait(Duration maxWait) {
        MaxWait = maxWait;
        return this;
    }

    public Duration getWhileCheckTime() {
        return whileCheckTime;
    }

    public Config setWhileCheckTime(Duration whileCheckTime) {
        this.whileCheckTime = whileCheckTime;
        return this;
    }

}