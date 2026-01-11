package com.codejune.socket;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.ObjectUtil;
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
            this.threadPoolExecutor.execute(() -> {
                try (
                        Socket socket = this.serverSocket.accept();
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream()
                ) {
                    Object response;
                    try {
                        response = this.listen(inputStream);
                    } catch (Throwable e) {
                        response = e.getMessage();
                    }
                    if (StringUtil.isEmpty(response)) {
                        response = " ";
                    }
                    try {
                        if (response instanceof InputStream responseInputStream) {
                            new OutputStreamWriter(outputStream).write(responseInputStream);
                        } else {
                            outputStream.write(ObjectUtil.toString(response).getBytes(StandardCharsets.UTF_8));
                        }
                        outputStream.flush();
                    } catch (Exception _) {}
                } catch (Throwable e) {
                    throw new BaseException(e);
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