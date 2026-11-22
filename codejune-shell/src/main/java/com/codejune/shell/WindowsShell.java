package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.Encoding;
import com.codejune.core.util.StringUtil;
import java.io.*;
import java.util.UUID;

/**
 * LocalShell
 *
 * @author ZJ
 */
public final class WindowsShell extends Shell {

    private final String intDirectory;

    private Process process;

    private BufferedReader inputStreamReader;

    private BufferedWriter bufferedWriter;

    private final String promptId = UUID.randomUUID().toString();

    public WindowsShell(String intDirectory) {
        this.intDirectory = intDirectory;
    }

    public WindowsShell() {
        this(null);
    }

    @Override
    public void init() {
        if (this.process != null) {
            return;
        }
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/D");
        String prompt = processBuilder.environment().getOrDefault("PROMPT", "$P$G");
        processBuilder.environment().put("PROMPT", "$E" + this.promptId + prompt + "$E" + this.promptId);
        processBuilder.redirectErrorStream(true);
        if (!StringUtil.isEmpty(this.intDirectory)) {
            processBuilder.directory(new File(this.intDirectory));
        }
        try {
            this.process = processBuilder.start();
            this.inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Encoding.NATIVE));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), Encoding.NATIVE));
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try {
            this.readUntilPromptMarker(null);
            this.readUntilPromptMarker(null);
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

    @Override
    public synchronized String command(String command) {
        this.init();
        try {
            String[] inputLines = command.split("\\r\\n|\\r|\\n", -1);
            this.asyncCommand(String.join("\r\n", inputLines));
            StringBuilder result = new StringBuilder();
            int nextInput = 0;
            while (nextInput < inputLines.length) {
                StringBuilder response = new StringBuilder();
                boolean atPrompt = this.readUntilPromptMarker(response);
                if (atPrompt) {
                    this.readUntilPromptMarker(null);
                    if (response.length() >= 2 && response.substring(response.length() - 2).equals("\r\n")) {
                        response.setLength(response.length() - 2);
                    }
                }
                String text = response.toString().replace("\r\n", "\n");
                String echo = inputLines[nextInput] + "\n";
                if (text.startsWith(echo) || text.equals(inputLines[nextInput])) {
                    nextInput++;
                    int offset = Math.min(echo.length(), text.length());
                    while (nextInput < inputLines.length &&
                            text.startsWith("More? " + inputLines[nextInput] + "\n", offset)) {
                        offset += "More? ".length() + inputLines[nextInput].length() + 1;
                        nextInput++;
                    }
                    result.append(text, offset, text.length());
                }
                if (!atPrompt) {
                    break;
                }
            }
            return result.toString();
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void close() {
        Closeable.closeNoError(this.inputStreamReader);
        Closeable.closeNoError(this.bufferedWriter);
        if (this.process != null) {
            this.process.destroy();
        }
    }

    /**
     * 异步执行
     *
     * @param command command
     * */
    public void asyncCommand(String command) {
        this.init();
        try {
            this.bufferedWriter.write(command);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    private boolean readUntilPromptMarker(StringBuilder output) throws IOException {
        String marker = "\033" + this.promptId;
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = this.inputStreamReader.read()) != -1) {
            line.append((char) c);
            if (c == '\n') {
                this.publish(line, output);
            } else if (line.length() >= marker.length() &&
                    line.indexOf(marker, line.length() - marker.length()) >= 0) {
                line.setLength(line.length() - marker.length());
                this.publish(line, output);
                return true;
            }
        }
        this.publish(line, output);
        return false;
    }

    private void publish(StringBuilder line, StringBuilder output) {
        if (!line.isEmpty()) {
            if (this.listener != null) {
                this.listener.accept(line.toString());
            }
            if (output != null) {
                output.append(line);
            }
            line.setLength(0);
        }
    }

}