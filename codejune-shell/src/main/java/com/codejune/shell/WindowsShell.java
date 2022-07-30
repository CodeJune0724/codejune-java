package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.common.exception.InfoException;
import com.codejune.common.listener.InputStreamListener;
import com.codejune.common.model.Charset;
import com.codejune.common.model.ResponseResult;
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
    public ResponseResult command(String command, InputStreamListener inputStreamListener) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        try {
            process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            inputStream = process.getInputStream();
            String success = IOUtil.toString(inputStream, Charset.GBK, inputStreamListener);
            errorStream = process.getErrorStream();
            String error = IOUtil.toString(errorStream, Charset.GBK);
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