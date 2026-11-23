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

    private final String promptMarker = "\033" + this.promptId;

    private static final String NEW_LINE = "\r\n";

    private static final String CONTINUATION_PROMPT = "More? ";

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
            this.inputStreamReader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), Encoding.NATIVE));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), Encoding.NATIVE));
            this.readThroughPromptMarker(true);
            this.readThroughPromptMarker(true);
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

    @Override
    public synchronized String command(String command) {
        this.init();
        String[] lines = command.split("\\r\\n|\\r|\\n", -1);
        StringBuilder result = new StringBuilder();
        try {
            int nextLine = 0;
            while (nextLine < lines.length && this.process.isAlive()) {
                String firstLine = lines[nextLine++];
                String id = UUID.randomUUID().toString();
                String start = "__CMD_START_" + id + "__";
                String end = "__CMD_END_" + id + "__";
                String body = firstLine.isBlank() || firstLine.stripLeading().startsWith(":") ? "rem" : firstLine;
                String wrapped = "for %# in (start run end) do @if %#==start (echo(" + start
                        + ") else if %#==end (echo(" + end + ") else " + body;
                this.writeLine(wrapped);
                this.awaitEcho(wrapped, firstLine);
                String startLine = start + NEW_LINE;
                String chunk;
                while ((chunk = this.readChunk(true)) != null && !chunk.equals(startLine)) {
                    if (this.isPrompt(chunk)) {
                        this.publish(chunk);
                        this.readThroughPromptMarker(true);
                        break;
                    }
                    if (chunk.equals(CONTINUATION_PROMPT)) {
                        this.publish(chunk);
                        if (nextLine < lines.length) {
                            this.writeLine(lines[nextLine++]);
                        }
                        String echo = this.readChunk(false);
                        if (echo != null) {
                            this.publish(echo);
                        }
                    } else {
                        this.append(result, chunk);
                    }
                }
                if (startLine.equals(chunk) && this.readCommandOutput(result, end + NEW_LINE)) {
                    this.finishCommand(id);
                }
            }
            return result.toString().replace(NEW_LINE, "\n");
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
            this.writeLine(command);
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

    private boolean readCommandOutput(StringBuilder result, String end) throws IOException {
        String chunk;
        while ((chunk = this.readChunk(false)) != null) {
            if (chunk.endsWith(end)) {
                this.append(result, chunk.substring(0, chunk.length() - end.length()));
                return true;
            }
            this.append(result, chunk);
        }
        return false;
    }

    private void finishCommand(String id) throws IOException {
        String probe = "@rem " + id;
        this.writeLine(probe);
        if (this.awaitEcho(probe, null)) {
            this.readThroughPromptMarker(false);
            this.readThroughPromptMarker(false);
        }
    }

    private boolean awaitEcho(String input, String replacement) throws IOException {
        String echo = input + NEW_LINE;
        boolean insidePrompt = false;
        boolean promptEnded = false;
        String chunk;
        while ((chunk = this.readChunk(false)) != null) {
            if (chunk.endsWith(echo)) {
                this.publish(chunk.substring(0, chunk.length() - echo.length()));
                if (replacement != null) {
                    this.publish(replacement + NEW_LINE);
                }
                return promptEnded;
            }
            if (this.isPrompt(chunk)) {
                insidePrompt = !insidePrompt;
                promptEnded = !insidePrompt;
            } else {
                promptEnded = false;
            }
            this.publish(chunk);
        }
        return false;
    }

    private void readThroughPromptMarker(boolean publish) throws IOException {
        String chunk;
        while ((chunk = this.readChunk(false)) != null) {
            if (publish) {
                this.publish(chunk);
            }
            if (this.isPrompt(chunk)) {
                return;
            }
        }
    }

    private String readChunk(boolean readingInput) throws IOException {
        StringBuilder chunk = new StringBuilder();
        int c;
        while ((c = this.inputStreamReader.read()) != -1) {
            chunk.append((char) c);
            if (c == '\n' || chunk.indexOf(this.promptMarker, chunk.length() - this.promptMarker.length()) >= 0
                    || (readingInput && CONTINUATION_PROMPT.contentEquals(chunk))) {
                break;
            }
        }
        return chunk.isEmpty() ? null : chunk.toString();
    }

    private boolean isPrompt(String chunk) {
        return chunk.endsWith(this.promptMarker);
    }

    private void append(StringBuilder result, String chunk) {
        String visible = chunk.replace(this.promptMarker, "");
        result.append(visible);
        this.publish(visible);
    }

    private void publish(String text) {
        String visible = text.replace(this.promptMarker, "");
        if (this.listener != null && !visible.isEmpty()) {
            this.listener.accept(visible);
        }
    }

    private void writeLine(String command) throws IOException {
        synchronized (this) {
            this.bufferedWriter.write(command);
            this.bufferedWriter.write(NEW_LINE);
            this.bufferedWriter.flush();
        }
    }

}