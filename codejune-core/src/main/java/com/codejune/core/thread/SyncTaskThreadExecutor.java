package com.codejune.core.thread;

/**
 * 异步任务执行器
 *
 * @author ZJ
 * */
public abstract class SyncTaskThreadExecutor {

    private boolean status = false;

    /**
     * 任务逻辑
     * */
    public abstract void handler();

    /**
     * 开始运行
     * */
    public final synchronized void run() {
        if (this.status) {
            return;
        }
        this.status = true;
        Thread.startVirtualThread(() -> {
            try {
                this.handler();
            } finally {
                this.stop();
            }
        });
    }

    /**
     * 停止
     * */
    public final void stop() {
        this.status = false;
    }

}