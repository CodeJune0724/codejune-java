package com.codejune;

import com.codejune.common.listener.InputStreamListener;
import com.codejune.common.model.ResponseResult;

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
     * @param inputStreamListener 监听器
     *
     * @return ResponseResult
     * */
    ResponseResult command(String command, InputStreamListener inputStreamListener);

    default ResponseResult command(String command) {
        return command(command, null);
    }

}