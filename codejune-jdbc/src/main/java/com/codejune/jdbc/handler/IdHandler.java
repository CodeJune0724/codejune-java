package com.codejune.jdbc.handler;

/**
 * IdHandler
 *
 * @author ZJ
 * */
public interface IdHandler {

    /**
     * 获取名称
     *
     * @return 名称
     * */
    String getName();

    /**
     * 获取序列名称
     *
     * @return 序列名称
     * */
    String getSequence();

}