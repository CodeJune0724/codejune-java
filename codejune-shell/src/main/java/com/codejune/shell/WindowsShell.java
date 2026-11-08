package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.Encoding;
import com.codejune.core.util.StringUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LocalShell
 *
 * @author ZJ
 */
public final class WindowsShell extends Shell {

    private final String intDirectory;

    private Process process;

    private InputStreamReader inputStreamReader;

    private BufferedWriter bufferedWriter;

    private static final String MARKER_PREFIX = "__CMD_DONE_";

    private static final String MARKER_SUFFIX = "__";

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
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe");
        processBuilder.redirectErrorStream(true);
        if (!StringUtil.isEmpty(this.intDirectory)) {
            processBuilder.directory(new File(this.intDirectory));
        }
        try {
            this.process = processBuilder.start();
            this.inputStreamReader = new InputStreamReader(process.getInputStream(), Encoding.NATIVE);
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), Encoding.NATIVE));
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try {
            int c;
            while ((c = this.inputStreamReader.read()) != -1) {
                if (this.listener != null) {
                    this.listener.accept(String.valueOf((char) c));
                }
                if (c == '>') {
                    break;
                }
            }
        } catch (IOException e) {
            throw new BaseException(e);
        }
    }

    @Override
    public synchronized String command(String command) {
        this.init();
        try {
            String marker = MARKER_PREFIX + UUID.randomUUID().toString().replace("-", "") + MARKER_SUFFIX;
            this.bufferedWriter.write(command);
            this.bufferedWriter.newLine();
            this.bufferedWriter.write("echo " + marker);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            List<String> resultList = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            boolean hitMarker = false;
            boolean done = false;
            while (!done) {
                int c = this.inputStreamReader.read();
                if (c == -1) {
                    break;
                }
                char ch = (char) c;
                if (hitMarker) {
                    if (ch == '>') {
                        line.append(ch);
                        if (this.listener != null) {
                            this.listener.accept(line.toString());
                        }
                        line.setLength(0);
                        done = true;
                    } else if (ch != '\r' && ch != '\n') {
                        line.append(ch);
                    }
                    continue;
                }
                if (ch == '\n') {
                    String l = line.toString();
                    line.setLength(0);
                    boolean isMarkerExec = l.trim().equals(marker);
                    boolean isMarkerEcho = !isMarkerExec && l.contains(marker);
                    if (isMarkerExec) {
                        hitMarker = true;
                    } else {
                        if (!isMarkerEcho) {
                            if (this.listener != null) {
                                this.listener.accept(l + "\r\n");
                            }
                            resultList.add(l);
                        }
                    }
                } else {
                    if (ch != '\r') {
                        line.append(ch);
                    }
                }
            }
            if (resultList.size() <= 1) {
                return "";
            }
            return String.join("\n", resultList.subList(1, resultList.size()));
        } catch (Exception e) {
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

}