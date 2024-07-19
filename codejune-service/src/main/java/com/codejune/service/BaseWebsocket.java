package com.codejune.service;

import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.StringUtil;
import jakarta.websocket.*;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map;

public class BaseWebsocket {

    private Session session;

    /**
     * onOpen
     *
     * @param session session
     * */
    @OnOpen
    public final void onOpen(Session session) {
        this.session = session;
        this.onOpen();
    }

    /**
     * onOpen
     * */
    public void onOpen() {}

    /**
     * onMessage
     *
     * @param message message
     * */
    @OnMessage
    public final void $onMessage(String message) {
        this.onMessage(message);
        Map<?, ?> messageMap = Json.parse(message, Map.class);
        String type = MapUtil.get(messageMap, "type", String.class);
        if (StringUtil.isEmpty(type)) {
            return;
        }
        this.onMessage(type, MapUtil.get(messageMap, "data", String.class));
    }

    /**
     * onMessage
     *
     * @param message message
     * */
    public void onMessage(String message) {}

    /**
     * onMessage
     *
     * @param byteBuffer byteBuffer
     * */
    @OnMessage
    public final void $onMessage(ByteBuffer byteBuffer) {
        this.onMessage(byteBuffer);
    }

    /**
     * onMessage
     *
     * @param byteBuffer byteBuffer
     * */
    public void onMessage(ByteBuffer byteBuffer) {}

    /**
     * onMessage
     *
     * @param type type
     * @param message message
     * */
    public void onMessage(String type, String message) {}

    /**
     * onClose
     * */
    @OnClose
    public final void $onClose() {
        this.onClose();
    }

    /**
     * onClose
     * */
    public void onClose() {}

    /**
     * onError
     *
     * @param throwable throwable
     * */
    @OnError
    public final void onError(Throwable throwable) {
        try {
            this.send("$error", throwable == null ? null : throwable.getMessage());
        } finally {
            this.close();
        }
    }

    /**
     * 发送
     *
     * @param message message
     * */
    public final void send(Object message) {
        if (this.session == null || !this.session.isOpen()) {
            return;
        }
        synchronized (this) {
            try {
                this.session.getBasicRemote().sendText(Json.toString(message));
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
        if (this.session == null || !this.session.isOpen() || byteBuffer == null) {
            return;
        }
        synchronized (this) {
            try {
                this.session.getBasicRemote().sendBinary(byteBuffer);
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 发送
     *
     * @param type type
     * @param message message
     * */
    public final void send(String type, Object message) {
        if (StringUtil.isEmpty(type)) {
            throw new BaseException("type is null");
        }
        this.send(MapUtil.asMap(
                new AbstractMap.SimpleEntry<>("type", type),
                new AbstractMap.SimpleEntry<>("data", message)
        ));
    }

    /**
     * 关闭
     * */
    public final void close() {
        try {
            this.session.close();
        } catch (Exception ignored) {}
    }

}