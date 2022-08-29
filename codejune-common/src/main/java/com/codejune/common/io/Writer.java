package com.codejune.common.io;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.InputStreamReader;
import com.codejune.common.listener.WriteListener;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 写入
 *
 * @author ZJ
 * */
public class Writer {

    protected final OutputStream outputStream;

    protected int writeSize = 1024;

    protected WriteListener writeListener = data -> {};

    protected Writer(OutputStream outputStream) {
        if (outputStream == null) {
            throw new InfoException("outputStream is null");
        }
        this.outputStream = outputStream;
    }

    public final void setWriteSize(int writeSize) {
        if (writeSize <= 0) {
            return;
        }
        this.writeSize = writeSize;
    }

    public final void setWriteListener(WriteListener writeListener) {
        if (writeListener == null) {
            return;
        }
        this.writeListener = writeListener;
    }

    /**
     * 写入
     *
     * @param dataBuffer dataBuffer
     * */
    public final void write(DataBuffer dataBuffer) {
        if (dataBuffer == null) {
            return;
        }
        try {
            this.outputStream.write(dataBuffer.getBytes(), 0, dataBuffer.getLength());
        } catch (Exception e) {
            throw new InfoException(e);
        }
        writeListener.listen(dataBuffer);
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
        inputStreamReader.setReadSize(writeSize);
        inputStreamReader.setReadListener(Writer.this::write);
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
        write(new DataBuffer(bytes, bytes.length));
    }

}