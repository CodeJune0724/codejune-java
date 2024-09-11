package com.codejune.shell;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;
import java.util.Properties;

/**
 * linux客户端模式（实时交互）
 *
 * @author ZJ
 * */
public abstract class LinuxClientShell implements Closeable {

    private final Session session;

    private final ChannelShell channelShell;

    private final OutputStream outputStream;

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
            this.channelShell = (ChannelShell) this.session.openChannel("shell");
            this.channelShell.setPty(true);
            OutputStream outputStream = this.outputStream();
            if (outputStream != null) {
                this.channelShell.setOutputStream(outputStream);
            }
            this.channelShell.connect();
            this.outputStream = this.channelShell.getOutputStream();
        }catch (Exception e) {
            throw new BaseException(host + ", 连接失败");
        }
    }

    /**
     * 输入
     *
     * @param command command
     * */
    public final void send(String command) {
        try {
            this.outputStream.write(command.getBytes());
            this.outputStream.flush();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 设置输出流
     *
     * @return OutputStream
     * */
    protected abstract OutputStream outputStream();

    @Override
    public void close() {
        try {
            if (this.channelShell != null) {
                this.channelShell.disconnect();
            }
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Exception ignored) {}
    }

}