package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.common.Closeable;
import com.codejune.common.Listener;
import com.codejune.common.exception.InfoException;
import com.codejune.common.ResponseResult;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.Properties;

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
            throw new InfoException(host + ", 连接失败");
        }
    }

    @Override
    public ResponseResult command(String command, Listener<String> listener) {
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            InputStream inputStream = channelExec.getInputStream();
            InputStream errStream = channelExec.getErrStream();
            String result = "";
            TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
            textInputStreamReader.setListener(listener);
            result = result + textInputStreamReader.getData();
            TextInputStreamReader errorTextInputStreamReader = new TextInputStreamReader(errStream);
            errorTextInputStreamReader.setListener(listener);
            result = result + errorTextInputStreamReader.getData();
            return ResponseResult.returnTrue(channelExec.getExitStatus(), null, result);
        }
        catch (Exception e) {
            throw new InfoException(e);
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