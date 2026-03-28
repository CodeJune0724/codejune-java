package com.codejune.socket;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.ObjectUtil;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class ClientSocket {

    private final String host;

    private final int port;

    private int timeout = 0;

    public ClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 设置超时时间
     *
     * @param timeout timeout
     * */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 发送
     *
     * @param request request
     * @param response response
     * */
    public void send(Object request, Consumer<InputStream> response) {
        try (
                Socket socket = new Socket(host, port);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
        ) {
            if (this.timeout > 0) {
                socket.setSoTimeout(this.timeout);
            }
            if (request instanceof InputStream requestInputStream) {
                new OutputStreamWriter(outputStream).write(requestInputStream);
            } else {
                outputStream.write(ObjectUtil.toString(request).getBytes(StandardCharsets.UTF_8));
            }
            outputStream.flush();
            socket.shutdownOutput();
            if (response != null) {
                response.accept(inputStream);
            }
        } catch (Throwable e) {
            throw new BaseException(e);
        }
    }

    /**
     * 发送
     *
     * @param request request
     *
     * @return response
     * */
    public String send(Object request) {
        AtomicReference<String> result = new AtomicReference<>();
        this.send(request, inputStream -> result.set(new TextInputStreamReader(inputStream).getData()));
        return result.get();
    }

}