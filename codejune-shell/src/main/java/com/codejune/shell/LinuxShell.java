package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.ResponseResult;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.StringUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * LinuxShell
 *
 * @author ZJ
 * */
public final class LinuxShell implements Shell, Closeable {

    private final Session session;

    public LinuxShell(String host, int port, String username, String password) {
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

    @Override
    public ResponseResult command(String command, Consumer<String> listener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            InputStream inputStream = channelExec.getInputStream();
            InputStream errStream = channelExec.getErrStream();
            AtomicReference<String> result = new AtomicReference<>();
            result.set("");
            TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
            textInputStreamReader.read(data -> {
                if (listener != null) {
                    listener.accept(data);
                }
                result.set(result.get() + data);
            });
            TextInputStreamReader errorTextInputStreamReader = new TextInputStreamReader(errStream);
            errorTextInputStreamReader.read(data -> {
                if (listener != null) {
                    listener.accept(data);
                }
                result.set(result.get() + data);
            });
            return ResponseResult.returnTrue(channelExec.getExitStatus(), null, result.get());
        }
        catch (Exception e) {
            throw new BaseException(e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    @Override
    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

}