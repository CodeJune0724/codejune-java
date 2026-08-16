package com.codejune.core.os;

import com.codejune.core.BaseException;
import com.codejune.core.Encoding;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 进程
 *
 * @author ZJ
 * */
public final class Process {

    private final ProcessHandle processHandle;

    private Process(ProcessHandle processHandle) {
        this.processHandle = processHandle;
    }

    /**
     * 获取pid
     *
     * @return pid
     * */
    public int getPid() {
        return ObjectUtil.parse(this.processHandle.pid(), int.class);
    }

    /**
     * 获取进程名
     *
     * @return 进程名
     * */
    public String getName() {
        Optional<String> command = this.processHandle.info().command();
        return command.map(s -> new File(s).getName()).orElse(null);
    }

    /**
     * 获取完整的进程名
     *
     * @return 完整的进程名
     * */
    public String getFullName() {
        Optional<String> command = this.processHandle.info().command();
        return command.orElse(null);
    }

    /**
     * 获取参数
     *
     * @return 参数
     * */
    public String getArgument() {
        int pid = this.getPid();
        if (OSType.getCurrentOSType() == OSType.WINDOWS_7) {
            String commandResult = fastCommand("wmic process get ProcessId,CommandLine /format:csv | findstr " + pid);
            if (commandResult == null) {
                return null;
            }
            for (String item : commandResult.split("\n")) {
                if (!item.endsWith("," + pid)) {
                    continue;
                }
                return item.replace("," + pid, "");
            }
            return null;
        } else {
            String commandResult = fastCommand("powershell -NoProfile -Command \"Get-CimInstance Win32_Process -Filter ProcessId=" + pid + " | Select-Object ProcessId, CommandLine | Format-Table -Wrap -AutoSize\"");
            if (commandResult == null) {
                return null;
            }
            commandResult = commandResult.replace("\n         ", "");
            for (String item : commandResult.split("\n")) {
                item = item.trim();
                if (!item.startsWith(pid + "")) {
                    continue;
                }
                return item.replace(pid + " ", "");
            }
            return null;
        }
    }

    /**
     * 获取子进程
     *
     * @return 子进程
     * */
    public Set<Process> getChild() {
        Set<Process> result = new HashSet<>();
        this.processHandle.children().forEach(processHandle -> result.add(new Process(processHandle)));
        return result;
    }

    /**
     * 获取所有的子进程
     *
     * @return 所有的子进程
     * */
    public Set<Process> getAllChild() {
        Set<Process> result = new HashSet<>();
        this.processHandle.descendants().forEach(processHandle -> result.add(new Process(processHandle)));
        return result;
    }

    /**
     * 通过pid获取
     *
     * @param pid pid
     *
     * @return Process
     * */
    public static Process getByPid(int pid) {
        Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
        return processHandle.map(Process::new).orElse(null);
    }

    /**
     * 通过进程名获取
     *
     * @param name name
     *
     * @return Set<Process>
     * */
    public static Set<Process> getByName(String name) {
        Set<Process> result = new HashSet<>();
        if (StringUtil.isEmpty(name)) {
            return result;
        }
        Stream<ProcessHandle> processHandleStream = ProcessHandle.allProcesses();
        processHandleStream.forEach(processHandle -> {
            Optional<String> command = processHandle.info().command();
            if (command.isEmpty()) {
                return;
            }
            if (command.get().endsWith(name)) {
                result.add(new Process(processHandle));
            }
        });
        return result;
    }

    private static String fastCommand(String command) {
        if (StringUtil.isEmpty(command)) {
            return null;
        }
        java.lang.Process process = null;
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