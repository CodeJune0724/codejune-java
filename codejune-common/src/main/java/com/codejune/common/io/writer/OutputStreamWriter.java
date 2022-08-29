package com.codejune.common.io.writer;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.DataBuffer;
import com.codejune.common.io.Writer;
import com.codejune.common.io.reader.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OutputStreamWriter
 *
 * @author ZJ
 * */
public final class OutputStreamWriter extends Writer {

    public final OutputStream outputStream;

    public OutputStreamWriter(OutputStream outputStream) {
        if (outputStream == null) {
            throw new InfoException("outputStream is null");
        }
        this.outputStream = outputStream;
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        inputStreamReader.setReadSize(writeSize);
        inputStreamReader.setInputStreamReadListener(this::write);
    }

    /**
     * 写入
     *
     * @param dataBuffer dataBuffer
     * */
    public void write(DataBuffer dataBuffer) {
        try {
            this.outputStream.write(dataBuffer.getBytes(), 0, dataBuffer.getLength());
        } catch (Exception e) {
            throw new InfoException(e);
        }
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