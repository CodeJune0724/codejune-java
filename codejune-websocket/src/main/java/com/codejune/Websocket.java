package com.codejune;

import com.codejune.core.BaseException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;

/**
 * Websocket
 *
 * @author ZJ
 * */
public abstract class Websocket {

    private final String url;

    private WebSocketClient webSocketClient;

    private CountDownLatch countDownLatch;

    public Websocket(String url) {
        this.url = url;
    }

    /**
     * onOpen
     * */
    public abstract void onOpen();

    /**
     * onMessage
     *
     * @param message message
     * */
    public abstract void onMessage(String message);

    /**
     * onClose
     * */
    public abstract void onClose();

    /**
     * onError
     *
     * @param throwable throwable
     * */
    public abstract void onError(Throwable throwable);

    /**
     * 发送消息
     * */
    public final void send(String message) {
        if (!this.isConnect()) {
            throw new BaseException("webSocket is not connect");
        }
        this.webSocketClient.send(message);
    }

    /**
     * 发送消息
     * */
    public final void send(byte[] message) {
        if (!this.isConnect()) {
            throw new BaseException("webSocket is not connect");
        }
        this.webSocketClient.send(message);
    }

    /**
     * 连接
     * */
    public final void connect() {
        if (this.isConnect()) {
            throw new BaseException("webSocket is connect");
        }
        URI uri;
        try {
            uri = new URI(this.url);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Websocket.this.onOpen();
            }
            @Override
            public void onMessage(String message) {
                Websocket.this.onMessage(message);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                try {
                    Websocket.this.onClose();
                } finally {
                    if (Websocket.this.countDownLatch != null) {
                        Websocket.this.countDownLatch.countDown();
                    }
                }
            }
            @Override
            public void onError(Exception exception) {
                Websocket.this.onError(exception);
            }
        };
        TrustManager[] trustManager = new TrustManager[] { new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {}
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {}
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {}
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {}
        }};
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManager, new SecureRandom());
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.webSocketClient.setSocketFactory(sslContext.getSocketFactory());
        this.webSocketClient.connect();
        this.countDownLatch = new CountDownLatch(1);
    }

    /**
     * 关闭
     * */
    public final void close() {
        try {
            if (this.webSocketClient == null) {
                return;
            }
            this.webSocketClient.close();
        } finally {
            if (this.countDownLatch != null) {
                this.countDownLatch.countDown();
            }
        }
    }

    /**
     * await
     * */
    public final void await() {
        if (this.countDownLatch == null) {
            return;
        }
        try {
            this.countDownLatch.await();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    private boolean isConnect() {
        return this.webSocketClient != null && this.webSocketClient.isOpen();
    }

}