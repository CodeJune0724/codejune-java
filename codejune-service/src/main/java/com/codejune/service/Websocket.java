package com.codejune.service;

import com.codejune.Json;
import com.codejune.common.ResponseResult;
import com.codejune.common.BaseException;
import jakarta.websocket.*;
import java.nio.ByteBuffer;

public class Websocket {

    private Session session;

    /**
     * onOpen
     *
     * @param session session
     * */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    /**
     * onMessage
     *
     * @param message message
     * */
    @OnMessage
    public void onMessage(String message) {}

    /**
     * onMessage
     *
     * @param byteBuffer byteBuffer
     * */
    @OnMessage
    public void onMessage(ByteBuffer byteBuffer) {}

    /**
     * onClose
     * */
    @OnClose
    public void onClose() {}

    /**
     * onError
     *
     * @param throwable throwable
     * */
    @OnError
    public void onError(Throwable throwable) {}

    /**
     * 发送
     *
     * @param message message
     * */
    public final void send(Object message) {
        if (session == null || !this.session.isOpen()) {
            return;
        }
        synchronized (this) {
            try {
                session.getBasicRemote().sendText(Json.toString(message));
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 发送
     *
     * @param byteBuffer byteBuffer
     * */
    public final void send(ByteBuffer byteBuffer) {
        if (session == null || !this.session.isOpen() || byteBuffer == null) {
            return;
        }
        synchronized (this) {
            try {
                session.getBasicRemote().sendBinary(byteBuffer);
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 发送成功
     *
     * @param message message
     * */
    public final void sendSuccess(Object message) {
        send(Json.toString(ResponseResult.returnTrue(message)));
    }

    /**
     * 发送失败
     *
     * @param message message
     * */
    public final void sendError(Object message) {
        send(Json.toString(ResponseResult.returnFalse(null, message, null)));
    }

    /**
     * 关闭
     * */
    public final void close() {
        try {
            session.close();
        } catch (Exception ignored) {}
    }

}