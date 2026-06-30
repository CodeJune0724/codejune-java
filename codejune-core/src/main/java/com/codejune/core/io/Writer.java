package com.codejune.core.io;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.reader.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 写入
 *
 * @author ZJ
 * */
public class Writer implements Closeable {

    private final OutputStream outputStream;

    private int writeSize = 1024;

    protected Writer(OutputStream outputStream) {
        if (outputStream == null) {
            throw new BaseException("outputStream is null");
        }
        this.outputStream = outputStream;
    }

    @Override
    public void close() {
        Closeable.closeNoError(this.outputStream);
    }

    /**
     * setWriteSize
     *
     * @param writeSize writeSize
     * */
    public final void setWriteSize(int writeSize) {
        if (writeSize <= 0) {
            return;
        }
        this.writeSize = writeSize;
    }

    /**
     * getWriteSize
     *
     * @return writeSize
     * */
    public final int getWriteSize() {
        return this.writeSize;
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
        try {
            this.outputStream.write(bytes);
            this.outputStream.flush();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 写入
     *
     * @param byteBuffer byteBuffer
     * */
    public final void write(ByteBuffer byteBuffer) {
        this.write(Reader.getByte(byteBuffer));
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
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            inputStreamReader.setReadSize(this.writeSize);
            inputStreamReader.read(Writer.this::write);
        }
    }

}