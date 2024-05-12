package com.codejune.service;

import com.codejune.Json;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ServerSentEvent
 *
 * @author ZJ
 * */
public abstract class ServerSentEvent extends SseEmitter {

    public ServerSentEvent() {
        super(-1L);
        Thread.ofVirtual().start(() -> {
            try {
                this.execute();
            } catch (Throwable e) {
                this.sendError(e.getMessage());
            } finally {
                this.close();
            }
        });
    }

    /**
     * 执行操作
     * */
    public abstract void execute();

    /**
     * 监听关闭
     *
     * @param runnable runnable
     * */
    public void onClose(Runnable runnable) {
        this.onCompletion(runnable);
    }

    /**
     * 发送
     *
     * @param type type
     * @param message message
     * */
    public final void send(String type, Object message) {
        try {
            this.send(SseEmitter.event().name(type).data(Json.toString(message)));
        } catch (Exception ignored) {}
    }

    /**
     * 发送失败
     *
     * @param message message
     * */
    public final void sendError(Object message) {
        this.send("$error", message);
    }

    /**
     * 关闭
     * */
    public final void close() {
        try {
            this.complete();
        } catch (Exception ignored) {}
    }

}