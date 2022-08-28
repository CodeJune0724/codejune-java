package com.codejune.common.io;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.BinaryReader;
import com.codejune.common.listener.ProgressListener;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 写入
 *
 * @author ZJ
 * */
public final class Writer {

    public final OutputStream outputStream;

    private int writeSize = 1024;

    public Writer(OutputStream outputStream) {
        if (outputStream == null) {
            throw new InfoException("outputStream is null");
        }
        this.outputStream = outputStream;
    }

    public void setWriteSize(int writeSize) {
        this.writeSize = writeSize;
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * @param progressListener progressListener
     * */
    public void write(InputStream inputStream, ProgressListener progressListener) {
        if (inputStream == null) {
            return;
        }
        BinaryReader binaryReader = new BinaryReader(inputStream);
        binaryReader.setReadSize(this.writeSize);
        binaryReader.read((bytes, size) -> {
            try {
                outputStream.write(bytes, 0, size);
            } catch (Exception e) {
                throw new InfoException(e);
            }
        }, progressListener);
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        write(inputStream, null);
    }

    /**
     * 写入
     *
     * @param bytes bytes
     * */
    public void write(byte[] bytes) {
        try {
            this.outputStream.write(bytes);
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}