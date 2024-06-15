package com.codejune.shell;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * linux客户端模式（实时交互）
 *
 * @author ZJ
 * */
public final class LinuxClientShell implements Closeable {

    private final Session session;

    private Channel channel;

    private OutputStream outputStream;

    private Consumer<String> listener = data -> {};

    public LinuxClientShell(String host, int port, String username, String password) {
        try {
            JSch.setConfig("kex", JSch.getConfig("kex") + ",diffie-hellman-group1-sha1");
            JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa,ssh-dss");
            JSch jSch = new JSch();
            this.session = jSch.getSession(username, host, port);
            this.session.setPassword(password);
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            this.session.connect();
        }catch (Exception e) {
            throw new BaseException(host + ", 连接失败");
        }
    }

    /**
     * 设置监听器
     *
     * @param listener listener
     * */
    public void setListener(Consumer<String> listener) {
        if (this.listener == null) {
            return;
        }
        this.listener = listener;
    }

    /**
     * 发送
     *
     * @param command command
     * */
    public void send(String command) {
        if (this.channel == null) {
            try {
                this.channel = this.session.openChannel("shell");
                channel.connect();
                this.outputStream = this.channel.getOutputStream();
            } catch (Exception e) {
                throw new BaseException(e);
            }
            Thread.ofVirtual().start(() -> {
                try (InputStream inputStream = this.channel.getInputStream()) {
                    TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
                    textInputStreamReader.read(this.listener);
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            });
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.outputStream);
            outputStreamWriter.write("\n".getBytes());
        }
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.outputStream);
        outputStreamWriter.write(command.getBytes());
    }

    @Override
    public void close() {
        try {
            if (this.channel != null) {
                this.channel.disconnect();
            }
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Exception ignored) {}
    }

}