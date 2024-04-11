package com.codejune.common.io;

import com.codejune.common.BaseException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Reader
 *
 * @author ZJ
 * */
public abstract class Reader<T> {

    protected final InputStream inputStream;

    protected int size = 1024;

    protected Consumer<T> listener = data -> {};

    protected Reader(InputStream inputStream) {
        if (inputStream == null) {
            throw new BaseException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    public final void setSize(int size) {
        if (size <= 0) {
            return;
        }
        this.size = size;
    }

    public final void setListener(Consumer<T> listener) {
        if (listener == null) {
            return;
        }
        this.listener = listener;
    }

    /**
     * 读取
     * */
    public abstract void read();

    /**
     * 获取大小
     *
     * @return 大小
     * */
    public final int getSize() {
        try {
            return this.inputStream.available();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}