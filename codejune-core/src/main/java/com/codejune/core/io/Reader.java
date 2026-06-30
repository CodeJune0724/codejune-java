package com.codejune.core.io;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Reader
 *
 * @author ZJ
 * */
public abstract class Reader implements Closeable {

    protected final InputStream inputStream;

    private int readSize = 1024;

    protected Reader(InputStream inputStream) {
        if (inputStream == null) {
            throw new BaseException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    @Override
    public void close() {
        Closeable.closeNoError(this.inputStream);
    }

    /**
     * getReadSize
     *
     * @return readSize
     * */
    public final int getReadSize() {
        return this.readSize;
    }

    /**
     * setReadSize
     *
     * @param readSize readSize
     * */
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
    public final void read(Consumer<ByteBuffer> consumer) {
        if (consumer == null) {
            consumer = _ -> {};
        }
        try {
            byte[] bytes = new byte[this.readSize];
            int size = this.inputStream.read(bytes, 0, this.readSize);
            while (size != -1) {
                consumer.accept(ByteBuffer.wrap(bytes, 0, size));
                size = this.inputStream.read(bytes, 0, this.readSize);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

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
     * @return byte[]
     * */
    public final byte[] getByte() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.read(byteBuffer -> {
            try {
                byteArrayOutputStream.write(getByte(byteBuffer));
            } catch (Exception e) {
                throw new BaseException(e);
            }
        });
        return byteArrayOutputStream.toByteArray();
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