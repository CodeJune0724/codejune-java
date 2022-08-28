package com.codejune.common.listener;

/**
 * 二进制读取监听器
 *
 * @author ZJ
 * */
public interface InputStreamReadListener {

    /**
     * 监听操作
     *
     * @param bytes 数据
     * @param size 数据大小
     * */
    void listen(byte[] bytes, int size);

}