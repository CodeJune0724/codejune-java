package com.codejune.shell;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.codejune.Shell;
import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.listener.InputStreamListener;
import com.codejune.common.model.Charset;
import com.codejune.common.model.ResponseResult;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;

/**
 * LinuxShell
 *
 * @author ZJ
 * */
public final class LinuxShell implements Shell, Closeable {

    private final Connection connection;

    public LinuxShell(String host, int port, String username, String password) {
        try {
            connection = new Connection(host, port);
            connection.connect();
            if (!connection.authenticateWithPassword(username, password)) {
                throw new InfoException("连接失败");
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public ResponseResult command(String command, InputStreamListener inputStreamListener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        Session session = null;
        try {
            session = this.connection.openSession();
            session.execCommand(command);
            String out = IOUtil.toString(session.getStdout(), Charset.UTF_8, inputStreamListener);
            if(StringUtil.isEmpty(out)){
                out = IOUtil.toString(session.getStderr(), Charset.UTF_8, inputStreamListener);
            }
            return ResponseResult.returnFalse(session.getExitStatus(), null, out);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

}