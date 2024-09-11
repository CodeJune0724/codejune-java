package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.ObjectUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Websocket
 *
 * @author ZJ
 * */
public abstract class Websocket implements Closeable {

    private final WebSocketClient webSocketClient;

    private final CountDownLatch countDownLatch;

    private final List<Throwable> throwableList = new ArrayList<>();

    public Websocket(String url) {
        this.countDownLatch = new CountDownLatch(1);
        URI uri;
        try {
            uri = new URI(url);
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
                    Websocket.this.countDownLatch.countDown();
                }
            }
            @Override
            public void onError(Exception exception) {
                Websocket.this.throwableList.add(exception);
                Websocket.this.close();
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
        if (url.startsWith("wss://")) {
            this.webSocketClient.setSocketFactory(sslContext.getSocketFactory());
        }
        this.webSocketClient.connect();
    }

    /**
     * onOpen
     * */
    protected abstract void onOpen();

    /**
     * onMessage
     *
     * @param message message
     * */
    protected abstract void onMessage(String message);

    /**
     * onClose
     * */
    protected abstract void onClose();

    /**
     * 发送消息
     *
     * @param message message
     * */
    public final void send(String message) {
        this.webSocketClient.send(message);
    }

    /**
     * 发送消息
     *
     * @param message message
     * */
    public final void send(byte[] message) {
        this.webSocketClient.send(message);
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
        if (!ObjectUtil.isEmpty(this.throwableList)) {
            throw new BaseException(this.throwableList);
        }
    }

    @Override
    public final void close() {
        try {
            this.webSocketClient.close();
        } catch (Exception ignored) {}
    }

}