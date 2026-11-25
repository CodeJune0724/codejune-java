package com.codejune.shell;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.Encoding;
import com.codejune.core.util.StringUtil;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

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

    private final String inputFileVariable = "__CJ_INPUT_" + this.promptId;

    private final Path inputFile = Path.of(System.getProperty("java.io.tmpdir"), this.inputFileVariable + ".cmd");

    private Charset consoleCharset;

    private static final String NEW_LINE = "\r\n";

    private static final String CONTINUATION_PROMPT = "More? ";

    private static final Pattern CODE_PAGE = Pattern.compile("[0-9]+\\s*$");

    private static final Pattern NON_ASCII = Pattern.compile("[^\\x00-\\x7f]+");

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
        processBuilder.environment().put(this.inputFileVariable, this.inputFile.toString());
        processBuilder.redirectErrorStream(true);
        if (!StringUtil.isEmpty(this.intDirectory)) {
            processBuilder.directory(new File(this.intDirectory));
        }
        try {
            this.consoleCharset = this.readConsoleCharset();
            this.process = processBuilder.start();
            this.inputStreamReader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), this.consoleCharset));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), this.consoleCharset));
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
            CommandInput input = this.prepareInput(lines);
            boolean showPrompt = input.clearVariables().isEmpty();
            int nextLine = 0;
            while (nextLine < lines.length && this.process.isAlive()) {
                String firstLine = lines[nextLine];
                String wireLine = input.lines()[nextLine++];
                String id = UUID.randomUUID().toString();
                String start = "__CMD_START_" + id + "__";
                String end = "__CMD_END_" + id + "__";
                String body = firstLine.isBlank() || firstLine.stripLeading().startsWith(":") ? "rem" : wireLine;
                String prefix = "for %# in (start run end) do @if %#==start (echo(" + start
                        + ") else if %#==end (echo(" + end + ") else ";
                this.writeLine(prefix + body);
                this.awaitEcho(prefix, firstLine, showPrompt);
                showPrompt = true;
                String startLine = start + NEW_LINE;
                boolean promptAlreadyRead = false;
                String chunk;
                while ((chunk = this.readChunk(true)) != null && !chunk.equals(startLine)) {
                    if (this.isPrompt(chunk)) {
                        this.publish(chunk);
                        this.readThroughPromptMarker(true);
                        promptAlreadyRead = true;
                        break;
                    }
                    if (chunk.equals(CONTINUATION_PROMPT)) {
                        this.publish(chunk);
                        String continuation = null;
                        if (nextLine < lines.length) {
                            continuation = lines[nextLine];
                            this.writeLine(input.lines()[nextLine++]);
                        }
                        String echo = this.readChunk(false);
                        if (echo != null) {
                            this.publish(continuation == null ? echo : continuation + NEW_LINE);
                        }
                    } else {
                        this.append(result, chunk);
                    }
                }
                String clearVariables = nextLine == lines.length ? input.clearVariables() : "";
                if (startLine.equals(chunk) && this.readCommandOutput(result, end + NEW_LINE, true)) {
                    this.finishCommand(id, clearVariables, false);
                } else if (promptAlreadyRead && !clearVariables.isEmpty()) {
                    this.finishCommand(id, clearVariables, true);
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

    private Charset readConsoleCharset() throws IOException {
        Process codePageProcess = new ProcessBuilder("cmd.exe", "/D", "/C", "chcp")
                .redirectErrorStream(true)
                .start();
        try (InputStream output = codePageProcess.getInputStream()) {
            String response = new String(output.readAllBytes(), StandardCharsets.US_ASCII);
            var codePage = CODE_PAGE.matcher(response);
            if (codePage.find()) {
                String number = codePage.group().strip();
                if (number.equals("65001")) {
                    return StandardCharsets.UTF_8;
                }
                if (Charset.isSupported("cp" + number)) {
                    return Charset.forName("cp" + number);
                }
                if (Charset.isSupported("windows-" + number)) {
                    return Charset.forName("windows-" + number);
                }
            }
            return Encoding.NATIVE;
        } finally {
            codePageProcess.destroy();
        }
    }

    private CommandInput prepareInput(String[] lines) throws IOException {
        String[] wireLines = lines.clone();
        StringBuilder assignments = new StringBuilder();
        StringBuilder clearVariables = new StringBuilder();
        int variableIndex = 0;
        String variablePrefix = "__CJ_" + this.promptId.substring(0, 8) + "_";
        for (int i = 0; i < lines.length; i++) {
            var matcher = NON_ASCII.matcher(lines[i]);
            if (!matcher.find()) {
                continue;
            }
            StringBuilder wireLine = new StringBuilder();
            do {
                String variable = variablePrefix + variableIndex++;
                assignments.append("@set \"").append(variable).append('=').append(matcher.group()).append('"').append(NEW_LINE);
                clearVariables.append("@set \"").append(variable).append("=\" & ");
                matcher.appendReplacement(wireLine, "%" + variable + "%");
            } while (matcher.find());
            matcher.appendTail(wireLine);
            wireLines[i] = wireLine.toString();
        }
        if (!assignments.isEmpty()) {
            this.loadInputFragments(assignments.toString());
        }
        return new CommandInput(wireLines, clearVariables.toString());
    }

    private void loadInputFragments(String assignments) throws IOException {
        String marker = "__CMD_INPUT_" + UUID.randomUUID() + "__";
        String loader = "\"%" + this.inputFileVariable + "%\" & @echo(" + marker;
        Files.createFile(this.inputFile);
        try {
            Files.writeString(this.inputFile, assignments, this.consoleCharset);
            this.writeLine(loader);
            this.awaitEcho(loader, null, true);
            StringBuilder output = new StringBuilder();
            if (!this.readCommandOutput(output, marker + NEW_LINE, false) || !output.isEmpty()) {
                throw new IOException("Could not load Unicode cmd input: " + output);
            }
        } finally {
            Files.deleteIfExists(this.inputFile);
        }
    }

    private record CommandInput(String[] lines, String clearVariables) {}

    private boolean readCommandOutput(StringBuilder result, String end, boolean publish) throws IOException {
        String chunk;
        while ((chunk = this.readChunk(false)) != null) {
            boolean atEnd = chunk.endsWith(end);
            String output = atEnd ? chunk.substring(0, chunk.length() - end.length()) : chunk;
            result.append(output.replace(this.promptMarker, ""));
            if (publish) {
                this.publish(output);
            }
            if (atEnd) {
                return true;
            }
        }
        return false;
    }

    private void finishCommand(String id, String clearVariables, boolean promptAlreadyRead) throws IOException {
        String probe = clearVariables + "@rem " + id;
        this.writeLine(probe);
        boolean promptEnded = this.awaitEcho(probe, null, true);
        if (promptEnded || promptAlreadyRead) {
            this.readThroughPromptMarker(false);
            this.readThroughPromptMarker(false);
        }
    }

    private boolean awaitEcho(String inputPrefix, String replacement, boolean publishBeforeEcho) throws IOException {
        boolean insidePrompt = false;
        boolean promptEnded = false;
        String chunk;
        while ((chunk = this.readChunk(false)) != null) {
            int echoStart = chunk.indexOf(inputPrefix);
            if (echoStart >= 0 && chunk.endsWith(NEW_LINE)) {
                if (publishBeforeEcho) {
                    this.publish(chunk.substring(0, echoStart));
                }
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
            if (publishBeforeEcho) {
                this.publish(chunk);
            }
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