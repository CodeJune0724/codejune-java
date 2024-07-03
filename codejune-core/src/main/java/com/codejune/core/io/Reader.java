package com.codejune.core.io;

import com.codejune.core.BaseException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Reader
 *
 * @author ZJ
 * */
public abstract class Reader<T> {

    protected final InputStream inputStream;

    private int readSize = 1024;

    protected Reader(InputStream inputStream) {
        if (inputStream == null) {
            throw new BaseException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    public int getReadSize() {
        return readSize;
    }

    public final void setReadSize(int readSize) {
        if (readSize <= 0) {
            return;
        }
        this.readSize = readSize;
    }

    /**
     * 读取
     *
     * @param consumer consumer
     * */
    public abstract void read(Consumer<T> consumer);

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

    /**
     * 获取byte[]
     *
     * @param byteBuffer byteBuffer
     *
     * @return byte[]
     * */
    public static byte[] getByte(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        int length = byteBuffer.remaining();
        byte[] result = new byte[length];
        byteBuffer.get(result, byteBuffer.position(), length);
        return result;
    }

}