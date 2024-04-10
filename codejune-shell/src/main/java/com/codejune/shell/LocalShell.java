package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.common.ResponseResult;
import com.codejune.common.BaseException;
import com.codejune.common.SystemOS;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * WindowsShell
 *
 * @author ZJ
 * */
public final class LocalShell implements Shell {

    @Override
    public ResponseResult command(String command, Consumer<String> listener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        try {
            if (SystemOS.getCurrentSystemOS() == SystemOS.WINDOWS) {
                process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            } else if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
                process = Runtime.getRuntime().exec(command);
            } else {
                throw new BaseException("系统不支持");
            }
            inputStream = process.getInputStream();
            TextInputStreamReader successTextInputStreamReader = new TextInputStreamReader(inputStream);
            successTextInputStreamReader.setListener(listener);
            String success = successTextInputStreamReader.getData();

            errorStream = process.getErrorStream();
            TextInputStreamReader errorTextInputStreamReader = new TextInputStreamReader(errorStream);
            errorTextInputStreamReader.setListener(listener);
            String error = errorTextInputStreamReader.getData();

            int i = process.exitValue();
            if (i == 0) {
                return ResponseResult.returnTrue(i, null, success);
            } else {
                return ResponseResult.returnFalse(i, null, error);
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(errorStream);
            if (process != null) {
                process.destroy();
            }
        }
    }

}