package com.codejune.common.io;

import com.codejune.common.BaseException;
import com.codejune.common.io.reader.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 写入
 *
 * @author ZJ
 * */
public class Writer {

    protected final OutputStream outputStream;

    protected int size = 1024;

    protected Consumer<ByteBuffer> listen = data -> {};

    protected Writer(OutputStream outputStream) {
        if (outputStream == null) {
            throw new BaseException("outputStream is null");
        }
        this.outputStream = outputStream;
    }

    public final void setSize(int size) {
        if (size <= 0) {
            return;
        }
        this.size = size;
    }

    public final void setListen(Consumer<ByteBuffer> consumer) {
        if (this.listen == null) {
            return;
        }
        this.listen = consumer;
    }

    /**
     * 写入
     *
     * @param byteBuffer byteBuffer
     * */
    public final void write(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        try {
            this.outputStream.write(byteBuffer.array(), 0, byteBuffer.limit());
        } catch (Exception e) {
            throw new BaseException(e);
        }
        listen.accept(byteBuffer);
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * */
    public final void write(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        inputStreamReader.setSize(size);
        inputStreamReader.setListener(Writer.this::write);
        inputStreamReader.read();
    }

    /**
     * 写入
     *
     * @param bytes bytes
     * */
    public final void write(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        write(ByteBuffer.wrap(bytes, 0, bytes.length));
    }

}