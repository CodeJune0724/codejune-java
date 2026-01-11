package com.codejune.socket;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.StringUtil;
import com.codejune.core.util.ThreadUtil;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ServerSocket implements AutoCloseable {

    private final java.net.ServerSocket serverSocket;

    private final ThreadPoolExecutor threadPoolExecutor;

    public ServerSocket(int port, int threadNumber) {
        try {
            this.serverSocket = new java.net.ServerSocket(port);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.threadPoolExecutor = ThreadUtil.getThreadPoolExecutor(threadNumber);
        this.runHandler();
        while (true) {
            if (this.serverSocket.isClosed()) {
                break;
            }
            Socket socket;
            try {
                socket = this.serverSocket.accept();
            } catch (Throwable e) {
                throw new BaseException(e);
            }
            this.threadPoolExecutor.execute(() -> {
                try {
                    Object response;
                    try {
                        response = this.listen(socket.getInputStream());
                    } catch (Exception e) {
                        response = e.getMessage();
                    }
                    if (StringUtil.isEmpty(response)) {
                        response = " ";
                    }
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        if (response instanceof String responseString) {
                            outputStream.write(responseString.getBytes(StandardCharsets.UTF_8));
                        }
                        if (response instanceof InputStream responseInputStream) {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                            outputStreamWriter.write(responseInputStream);
                        }
                        outputStream.flush();
                    } catch (Exception _) {}
                } finally {
                    Closeable.closeNoError(socket);
                }
            });
        }
        this.close();
    }

    @Override
    public void close() {
        Closeable.closeNoError(serverSocket);
        Closeable.closeNoError(this.threadPoolExecutor);
    }

    /**
     * 监听
     *
     * @param inputStream 输入
     *
     * @return 输出
     * */
    public abstract Object listen(InputStream inputStream);

    /**
     * 运行后的处理
     * */
    public void runHandler() {}

    /**
     * 获取端口
     *
     * @return 端口
     * */
    public final int getPort() {
        return this.serverSocket.getLocalPort();
    }

}