package com.codejune.common.io;

import com.codejune.common.exception.InfoException;
import com.codejune.common.listener.ReadListener;
import java.io.InputStream;

/**
 * Reader
 *
 * @author ZJ
 * */
public abstract class Reader<T> {

    protected final InputStream inputStream;

    protected int readSize = 1024;

    protected ReadListener<T> readListener = data -> {};

    protected Reader(InputStream inputStream) {
        if (inputStream == null) {
            throw new InfoException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    public final void setReadSize(int readSize) {
        if (readSize <= 0) {
            return;
        }
        this.readSize = readSize;
    }

    public final void setReadListener(ReadListener<T> readListener) {
        if (readListener == null) {
            return;
        }
        this.readListener = readListener;
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
            throw new InfoException(e);
        }
    }

}