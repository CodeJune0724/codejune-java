package com.codejune.common;

/**
 * Closeable
 *
 * @author ZJ
 * */
public interface Closeable extends java.io.Closeable {

    /**
     * 关闭
     * */
    void close();

}