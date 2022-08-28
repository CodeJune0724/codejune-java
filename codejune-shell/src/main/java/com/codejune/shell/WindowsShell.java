package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.common.ResponseResult;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextReader;
import com.codejune.common.listener.TextReadListener;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.InputStream;

/**
 * WindowsShell
 *
 * @author ZJ
 * */
public final class WindowsShell implements Shell {

    @Override
    public ResponseResult command(String command, TextReadListener textReadListener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        if (textReadListener == null) {
            textReadListener = data -> {};
        }
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        final TextReadListener finalTextReadListener = textReadListener;
        try {
            process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            inputStream = process.getInputStream();
            final String[] success = {""};
            TextReader textReader = new TextReader(inputStream);
            textReader.read(data -> {
                finalTextReadListener.listen(data);
                success[0] = StringUtil.append(success[0], data);
            });
            errorStream = process.getErrorStream();
            final String[] error = {""};
            TextReader errorTextReader = new TextReader(errorStream);
            errorTextReader.read(data -> {
                finalTextReadListener.listen(data);
                error[0] = StringUtil.append(error[0], data);
            });
            int i = process.exitValue();
            if (i == 0) {
                return ResponseResult.returnTrue(i, null, success[0]);
            } else {
                return ResponseResult.returnFalse(i, null, error[0]);
            }
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(errorStream);
            if (process != null) {
                process.destroy();
            }
        }
    }

}