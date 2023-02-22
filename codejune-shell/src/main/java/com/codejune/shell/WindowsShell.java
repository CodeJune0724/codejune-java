package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.common.ResponseResult;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.listener.ReadListener;
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
    public ResponseResult command(String command, ReadListener<String> readListener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        try {
            process = Runtime.getRuntime().exec("cmd.exe /c " + command);

            inputStream = process.getInputStream();
            TextInputStreamReader successTextInputStreamReader = new TextInputStreamReader(inputStream);
            successTextInputStreamReader.setReadListener(readListener);
            String success = successTextInputStreamReader.getData();

            errorStream = process.getErrorStream();
            TextInputStreamReader errorTextInputStreamReader = new TextInputStreamReader(errorStream);
            errorTextInputStreamReader.setReadListener(readListener);
            String error = errorTextInputStreamReader.getData();

            int i = process.exitValue();
            if (i == 0) {
                return ResponseResult.returnTrue(i, null, success);
            } else {
                return ResponseResult.returnFalse(i, null, error);
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