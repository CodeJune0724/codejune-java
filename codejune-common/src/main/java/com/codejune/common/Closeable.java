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

    /**
     * 无异常关闭
     *
     * @param closeable closeable
     * */
    static void closeNoError(Closeable closeable) {
        try {
            closeable.close();
        } catch (Throwable ignored) {}
    }

}