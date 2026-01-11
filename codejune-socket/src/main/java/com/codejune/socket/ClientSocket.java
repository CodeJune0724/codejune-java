package com.codejune.socket;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
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

    private final Socket socket;

    public ClientSocket(String host, int port) {
        try {
            this.socket = new Socket(host, port);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 发送
     *
     * @param request request
     * @param response response
     * */
    public void send(Object request, Consumer<InputStream> response) {
        try {
            OutputStream outputStream = this.socket.getOutputStream();
            if (request instanceof InputStream requestInputStream) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(requestInputStream);
            } else {
                outputStream.write(ObjectUtil.toString(request).getBytes(StandardCharsets.UTF_8));
            }
            outputStream.flush();
            this.socket.shutdownOutput();
            if (response != null) {
                response.accept(this.socket.getInputStream());
            }
        } catch (Throwable e) {
            throw new BaseException(e);
        } finally {
            Closeable.closeNoError(this.socket);
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