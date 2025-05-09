package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.ResponseResult;
import com.codejune.core.BaseException;
import com.codejune.core.SystemOS;
import com.codejune.core.util.StringUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        try {
            StringBuilder result = new StringBuilder();
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (SystemOS.getCurrentSystemOS() == SystemOS.WINDOWS) {
                processBuilder.command("cmd.exe", "/c", command);
            } else if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
                processBuilder.command("/bin/bash", "-c", command);
            } else {
                throw new BaseException("系统不支持");
            }
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            try (InputStream inputStream = process.getInputStream()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, System.getProperties().get("sun.jnu.encoding").toString()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            int i = process.waitFor();
            String resultString = result.toString();
            if (!StringUtil.isEmpty(resultString)) {
                resultString = resultString.substring(0, resultString.length() - 1);
            }
            if (i == 0) {
                return ResponseResult.returnTrue(i, null, resultString);
            } else {
                return ResponseResult.returnFalse(i, null, resultString);
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}