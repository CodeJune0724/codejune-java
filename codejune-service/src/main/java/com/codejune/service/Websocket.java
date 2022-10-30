package com.codejune.service;

import com.codejune.common.ResponseResult;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.JsonUtil;
import javax.websocket.*;
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
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(JsonUtil.toJsonString(message));
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 发送
     *
     * @param byteBuffer byteBuffer
     * */
    public final void send(ByteBuffer byteBuffer) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendBinary(byteBuffer);
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 发送成功
     *
     * @param message message
     * */
    public final void sendSuccess(Object message) {
        send(JsonUtil.toJsonString(ResponseResult.returnTrue(message)));
    }

    /**
     * 发送失败
     *
     * @param message message
     * */
    public final void sendError(Object message) {
        send(JsonUtil.toJsonString(ResponseResult.returnFalse(null, message, null)));
    }

    /**
     * 关闭
     * */
    public final void close() {
        try {
            session.close();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}