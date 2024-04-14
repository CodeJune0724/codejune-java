package com.codejune.service;

import com.codejune.Json;
import com.codejune.common.BaseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ServerSentEvent
 *
 * @author ZJ
 * */
public final class ServerSentEvent extends SseEmitter {

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
    public void send(String type, Object message) {
        try {
            this.send(SseEmitter.event().name(type).data(Json.toString(message)));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 发送失败
     *
     * @param message message
     * */
    public void sendError(Object message) {
        this.send("$error", message);
    }

    /**
     * 关闭
     * */
    public void close() {
        this.complete();
    }

}