package com.codejune;

import com.codejune.core.util.ShellUtil;
import java.io.Closeable;
import java.util.function.Consumer;

/**
 * Shell
 *
 * @author ZJ
 * */
public abstract class Shell implements Closeable {

    protected Consumer<String> listener;

    /**
     * 初始化
     * */
    public abstract void init();

    /**
     * 发送指令
     *
     * @param command 指令
     *
     * @return 输出
     * */
    public abstract String command(String command);

    /**
     * setListener
     *
     * @param listener listener
     * */
    public final void setListener(Consumer<String> listener) {
        this.listener = listener;
    }

    /**
     * 快速发送指令
     *
     * @param command 指令
     *
     * @return 输出
     * */
    public static String fastCommand(String command) {
        return ShellUtil.fastCommand(command);
    }

}