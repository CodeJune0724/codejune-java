package com.codejune;

import com.codejune.common.Listener;
import com.codejune.common.ResponseResult;

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
    ResponseResult command(String command, Listener<String> listener);

    default ResponseResult command(String command) {
        return command(command, null);
    }

}