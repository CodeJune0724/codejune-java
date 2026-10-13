package com.codejune.system;

import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * 进程
 *
 * @author ZJ
 * */
public final class Process {

    private final ProcessHandle processHandle;

    private static final OperatingSystem OPERATING_SYSTEM = new oshi.nativefree.SystemInfo().getOperatingSystem();

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
        List<String> argumentList = getArgument(pid);
        if (ObjectUtil.isEmpty(argumentList)) {
            return null;
        }
        return argumentList.getFirst();
    }

    /**
     * 获取父级进程
     *
     * @return Process
     * */
    public Process getParent() {
        Optional<ProcessHandle> parent = this.processHandle.parent();
        return parent.map(Process::new).orElse(null);
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
     * 是否有效
     *
     * @return 是否有效
     * */
    public boolean isAlive() {
        return this.processHandle.isAlive();
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

    /**
     * 获取参数
     *
     * @param query query
     *
     * @return 参数
     * */
    public static List<String> getArgument(Object query) {
        List<String> result = new ArrayList<>();
        if (query instanceof Integer integer) {
            OSProcess osProcess = OPERATING_SYSTEM.getProcess(integer);
            if (osProcess != null) {
                result.add(osProcess.getCommandLine());
            }
        }
        if (query instanceof String string) {
            for (OSProcess osProcess : OPERATING_SYSTEM.getProcesses()) {
                if (string.equalsIgnoreCase(osProcess.getName() + ".exe")) {
                    result.add(osProcess.getProcessID() + "\t" + osProcess.getCommandLine());
                }
            }
        }
        return result;
    }

}