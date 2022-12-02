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
    void close();

    /**
     * 关闭
     *
     * @param closeable closeable
     * */
    static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        closeable.close();
    }

}