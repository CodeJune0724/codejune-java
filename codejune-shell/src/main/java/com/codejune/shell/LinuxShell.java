package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ThreadUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * LinuxShell
 *
 * @author ZJ
 * */
public final class LinuxShell extends Shell {

    private final Session session;

    private ChannelShell channelShell;

    private InputStreamReader inputStreamReader;

    private BufferedWriter bufferedWriter;

    public LinuxShell(String host, int port, String username, String password) {
        try {
            JSch.setConfig("kex", JSch.getConfig("kex") + ",diffie-hellman-group1-sha1");
            JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa,ssh-dss");
            JSch jSch = new JSch();
            this.session = jSch.getSession(username, host, port);
            this.session.setPassword(password.getBytes(StandardCharsets.UTF_8));
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            this.session.connect();
        }catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void init() {
        if (this.channelShell != null) {
            return;
        }
        try {
            this.channelShell = (ChannelShell) this.session.openChannel("shell");
            this.channelShell.setPty(true);
            this.channelShell.connect();
            this.inputStreamReader = new InputStreamReader(this.channelShell.getInputStream());
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.channelShell.getOutputStream()));
            this.getResponse();
        }catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public synchronized String command(String command) {
        this.init();
        try {
            this.bufferedWriter.write(command);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            List<String> result = ArrayUtil.asList(this.getResponse().split("\n"));
            if (!result.isEmpty()) {
                result.removeFirst();
            }
            if (!result.isEmpty()) {
                result.removeLast();
            }
            return ArrayUtil.toString(result, s -> s, "\n");
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void close() {
        Closeable.closeNoError(this.inputStreamReader);
        Closeable.closeNoError(this.bufferedWriter);
        try {
            if (this.channelShell != null) {
                this.channelShell.disconnect();
            }
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Exception ignored) {}
    }

    private String getResponse() {
        try {
            StringBuilder result = new StringBuilder();
            while (!this.inputStreamReader.ready()) {
                ThreadUtil.sleep(100);
            }
            while (true) {
                if (result.toString().endsWith("# ") && !this.inputStreamReader.ready()) {
                    break;
                }
                int read = this.inputStreamReader.read();
                if (read == -1) {
                    break;
                }
                String item = String.valueOf((char) read);
                if (this.listener != null) {
                    this.listener.accept(item);
                }
                result.append(item);
            }
            return result.toString();
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

}