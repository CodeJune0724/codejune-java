package com.codejune;

import com.codejune.core.ResponseResult;
import java.util.function.Consumer;

/**
 * Shell
 *
 * @author ZJ
 * */
public interface Shell {

    /**
     * 发送指令
     *
     * @param command 指令
     * @param listener listener
     *
     * @return ResponseResult
     * */
    ResponseResult command(String command, Consumer<String> listener);

    default ResponseResult command(String command) {
        return command(command, null);
    }

}