package com.codejune;

import com.codejune.common.ResponseResult;
import com.codejune.common.listener.ReadListener;

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
     * @param readListener 监听器
     *
     * @return ResponseResult
     * */
    ResponseResult command(String command, ReadListener<String> readListener);

    default ResponseResult command(String command) {
        return command(command, null);
    }

}