package com.codejune.core.util;

import com.codejune.core.BaseException;
import com.codejune.core.Encoding;
import com.codejune.core.os.OSType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ShellUtil
 *
 * @author ZJ
 * */
public final class ShellUtil {

    /**
     * 快速发送指令
     *
     * @param command 指令
     *
     * @return 输出
     * */
    public static String fastCommand(String command) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        Process process = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (OSType.getCurrentOSType().isWindows()) {
                processBuilder.command("cmd.exe", "/c", command);
            } else if (OSType.getCurrentOSType() == OSType.LINUX) {
                processBuilder.command("/bin/bash", "-c", command);
            } else {
                throw new BaseException("系统不支持");
            }
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            try (InputStream inputStream = process.getInputStream()) {
                BufferedReader bufferedReader;
                if (OSType.getCurrentOSType().isWindows()) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Encoding.NATIVE));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            String result = stringBuilder.toString();
            if (!StringUtil.isEmpty(result)) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}