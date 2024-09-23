package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.Closeable;
import com.codejune.core.ResponseResult;
import com.codejune.core.BaseException;
import com.codejune.core.SystemOS;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.StringUtil;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (SystemOS.getCurrentSystemOS() == SystemOS.WINDOWS) {
                processBuilder.command("cmd.exe", "/c", command);
            } else if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
                processBuilder.command("/bin/bash", "-c", command);
            } else {
                throw new BaseException("系统不支持");
            }
            process = processBuilder.start();
            Function<InputStream, String> readFunction = (inputStreamData) -> {
                AtomicReference<String> result = new AtomicReference<>();
                TextInputStreamReader successTextInputStreamReader = new TextInputStreamReader(inputStreamData);
                successTextInputStreamReader.read(data -> {
                    if (listener != null) {
                        listener.accept(data);
                    }
                    result.set(result.get() + data);
                });
                return result.get();
            };
            inputStream = process.getInputStream();
            String success = readFunction.apply(inputStream);
            errorStream = process.getErrorStream();
            String error = readFunction.apply(errorStream);
            int i = process.waitFor();
            if (i == 0) {
                return ResponseResult.returnTrue(i, null, success);
            } else {
                return ResponseResult.returnFalse(i, null, error);
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            Closeable.closeNoError(inputStream);
            Closeable.closeNoError(errorStream);
            if (process != null) {
                process.destroy();
            }
        }
    }

}