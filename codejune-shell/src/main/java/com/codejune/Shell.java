package com.codejune;

import com.codejune.common.ResponseResult;
import com.codejune.common.listener.TextReadListener;

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
     * @param textReadListener 监听器
     *
     * @return ResponseResult
     * */
    ResponseResult command(String command, TextReadListener textReadListener);

    default ResponseResult command(String command) {
        return command(command, null);
    }

}