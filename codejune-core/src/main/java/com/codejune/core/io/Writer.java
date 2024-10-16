package com.codejune.core.io;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 写入
 *
 * @author ZJ
 * */
public class Writer {

    protected final OutputStream outputStream;

    protected int size = 1024;

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
            this.outputStream.flush();
        } catch (Exception e) {
            throw new BaseException(e);
        }
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
        inputStreamReader.setReadSize(size);
        inputStreamReader.read(Writer.this::write);
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