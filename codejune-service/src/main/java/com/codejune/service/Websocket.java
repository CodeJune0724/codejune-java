package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.JsonUtil;
import javax.websocket.*;
import java.nio.ByteBuffer;

public class Websocket {

    /**
     * onOpen
     *
     * @param session session
     * */
    @OnOpen
    public void onOpen(Session session) {}

    /**
     * onMessage
     *
     * @param session session
     * @param message message
     * */
    @OnMessage
    public void onMessage(Session session, String message) {}

    /**
     * onMessage
     *
     * @param session session
     * @param byteBuffer byteBuffer
     * */
    @OnMessage
    public void onMessage(Session session, ByteBuffer byteBuffer) {}

    /**
     * onClose
     *
     * @param session session
     * */
    @OnClose
    public void onClose(Session session) {}

    /**
     * onError
     *
     * @param session session
     * @param throwable throwable
     * */
    @OnError
    public void onError(Session session, Throwable throwable) {}

    /**
     * 发送
     *
     * @param session session
     * @param message message
     * */
    public final void send(Session session, String message) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 发送
     *
     * @param session session
     * @param byteBuffer byteBuffer
     * */
    public final void send(Session session, ByteBuffer byteBuffer) {
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
     * 发送
     *
     * @param session session
     * @param object byteBuffer
     * */
    public final void sendToJson(Session session, Object object) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(JsonUtil.toJsonString(object));
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}