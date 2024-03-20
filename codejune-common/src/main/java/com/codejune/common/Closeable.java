package com.codejune.common;

/**
 * Closeable
 *
 * @author ZJ
 * */
public interface Closeable extends AutoCloseable {

    /**
     * 关闭
     * */
    @Override
    void close();

}