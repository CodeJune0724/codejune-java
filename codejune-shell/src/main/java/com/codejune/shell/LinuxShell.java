package com.codejune.shell;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.codejune.Shell;
import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.ResponseResult;
import com.codejune.common.io.reader.TextReader;
import com.codejune.common.listener.TextReadListener;
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
    public ResponseResult command(String command, TextReadListener textReadListener) {
        if (StringUtil.isEmpty(command)) {
            return new ResponseResult();
        }
        if (textReadListener == null) {
            textReadListener = data -> {};
        }
        Session session = null;
        InputStream inputStream = null;
        final TextReadListener finalTextReadListener = textReadListener;
        try {
            session = this.connection.openSession();
            session.execCommand(command);
            inputStream = session.getStdout();
            final String[] out = {""};
            if (inputStream != null) {
                TextReader textReader = new TextReader(inputStream);
                textReader.read(data -> {
                    finalTextReadListener.listen(data);
                    out[0] = StringUtil.append(out[0], data);
                });
            }
            if (StringUtil.isEmpty(out[0])) {
                inputStream = session.getStderr();
                TextReader textReader = new TextReader(inputStream);
                textReader.read(data -> {
                    finalTextReadListener.listen(data);
                    out[0] = StringUtil.append(out[0], data);
                });
            }
            return ResponseResult.returnFalse(session.getExitStatus(), null, out[0]);
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