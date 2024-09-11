package com.codejune.shell;

import com.codejune.core.BaseException;
import com.codejune.core.util.ThreadUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * linux客户端模式（实时交互）
 *
 * @author ZJ
 * */
public final class LinuxClientShell {

    private final Session session;

    private final Channel channel;

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


            ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
            channel.setPty(true);
            channel.connect();

            new Thread(() -> {
                try {
                    InputStream inputStream = channel.getInputStream();
                    while (true) {
                        while (inputStream.available() > 0) {
                            byte[] bytes = new byte[inputStream.available()];
                            inputStream.read(bytes);
                            System.out.print(new String(bytes, StandardCharsets.UTF_8));
                        }
                    }
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }).start();

            OutputStream outputStream = channel.getOutputStream();
//            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//
            ThreadUtil.sleep(1000);

            for (String item : "ipconfig".split("")) {
                outputStream.write((item).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                ThreadUtil.sleep(1000);
            }
            outputStream.write("\b".getBytes(StandardCharsets.UTF_8));
            ThreadUtil.sleep(1000);
            outputStream.flush();

            outputStream.write("\b".getBytes(StandardCharsets.UTF_8));
            ThreadUtil.sleep(1000);
            outputStream.flush();

            outputStream.write("\b".getBytes(StandardCharsets.UTF_8));
            ThreadUtil.sleep(1000);
            outputStream.flush();


//            outputStream.write("cd /\r".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//            ThreadUtil.sleep(1000);
//
//            outputStream.write("ll /\r".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//            ThreadUtil.sleep(1000);
//
//            outputStream.write("ls\n".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//            ThreadUtil.sleep(3000);
//
//            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//            ThreadUtil.sleep(500);

//            outputStream.write("cd app\t\n".getBytes(StandardCharsets.UTF_8));
//            outputStream.flush();
//            ThreadUtil.sleep(500);
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
//
//    /**
//     * 发送
//     *
//     * @param command command
//     * */
//    public void send(String command) {
//        if (this.channel == null) {
//            try {
//                this.channel = this.session.openChannel("shell");
//                channel.connect();
//                this.outputStream = this.channel.getOutputStream();
//            } catch (Exception e) {
//                throw new BaseException(e);
//            }
//            Thread.ofVirtual().start(() -> {
//                try (InputStream inputStream = this.channel.getInputStream()) {
//                    TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
//                    textInputStreamReader.read(this.listener);
//                } catch (Exception e) {
//                    throw new BaseException(e);
//                }
//            });
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.outputStream);
//            outputStreamWriter.write("\n".getBytes());
//        }
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.outputStream);
//        outputStreamWriter.write(command.getBytes());
//    }

    @Override
    public void close() {
        try {
//            if (this.channel != null) {
//                this.channel.disconnect();
//            }
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Exception ignored) {}
    }

}