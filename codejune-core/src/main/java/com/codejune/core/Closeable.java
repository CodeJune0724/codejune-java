package com.codejune.core;

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

    /**
     * 无异常关闭
     *
     * @param autoCloseable autoCloseable
     * */
    static void closeNoError(AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable ignored) {}
    }

}