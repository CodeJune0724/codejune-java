package com.codejune.shell;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.codejune.Shell;
import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.ResponseResult;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.listener.ReadListener;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.InputStream;

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
    public ResponseResult command(String command, ReadListener<String> readListener) {
        if (StringUtil.isEmpty(command)) {
            return new ResponseResult();
        }
        Session session = null;
        InputStream inputStream = null;
        try {
            session = this.connection.openSession();
            session.execCommand(command);
            inputStream = session.getStdout();
            String out = null;
            if (inputStream != null) {
                TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
                textInputStreamReader.setReadListener(readListener);
                out = textInputStreamReader.getData();
            }
            if (StringUtil.isEmpty(out)) {
                inputStream = session.getStderr();
                TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
                textInputStreamReader.setReadListener(readListener);
                out = textInputStreamReader.getData();
            }
            return ResponseResult.returnFalse(session.getExitStatus(), null, out);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
            IOUtil.close(inputStream);
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

}