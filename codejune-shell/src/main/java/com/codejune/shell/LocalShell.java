package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ThreadUtil;
import java.io.*;
import java.util.List;

/**
 * LocalShell
 *
 * @author ZJ
 * */
public final class LocalShell extends Shell {

    private Process process;

    private InputStreamReader inputStreamReader;

    private BufferedWriter bufferedWriter;

    public LocalShell() {}

    @Override
    public void open() {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe");
        processBuilder.redirectErrorStream(true);
        try {
            this.process = processBuilder.start();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.inputStreamReader = new InputStreamReader(process.getInputStream());
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.getResponse();
    }

    @Override
    public String command(String command) {
        try {
            this.bufferedWriter.write(command);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            List<String> result = ArrayUtil.asList(this.getResponse().split("\n"));
            if (!result.isEmpty()) {
                result.removeFirst();
            }
            if (!result.isEmpty()) {
                result.removeLast();
            }
            return ArrayUtil.toString(result, s -> s, "\n");
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void close() {
        Closeable.closeNoError(this.inputStreamReader);
        Closeable.closeNoError(this.bufferedWriter);
        this.process.destroy();
    }

    private String getResponse() {
        try {
            StringBuilder result = new StringBuilder();
            while (!this.inputStreamReader.ready()) {
                ThreadUtil.sleep(100);
            }
            while (true) {
                if (result.toString().endsWith(">") && !this.inputStreamReader.ready()) {
                    break;
                }
                int read = this.inputStreamReader.read();
                if (read == -1) {
                    break;
                }
                String item = String.valueOf((char) read);
                if (this.listener != null) {
                    this.listener.accept(item);
                }
                result.append(item);
            }
            return result.toString();
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

}