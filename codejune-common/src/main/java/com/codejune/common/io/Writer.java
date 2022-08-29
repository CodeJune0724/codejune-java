package com.codejune.common.io;

import com.codejune.common.Progress;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.InputStreamReader;
import com.codejune.common.listener.ProgressListener;
import com.codejune.common.listener.ReadListener;

import java.io.InputStream;

/**
 * 写入
 *
 * @author ZJ
 * */
public abstract class Writer {

    protected int writeSize = 1024;

    public final void setWriteSize(int writeSize) {
        if (writeSize <= 0) {
            return;
        }
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
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        inputStreamReader.setReadSize(writeSize);
        Progress progress = null;
        if (progressListener != null) {
            final ProgressListener finalProgressListener = progressListener;
            progress = new Progress(inputStreamReader.getSize()) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
        }
        inputStreamReader.setReadListener(new ReadListener<DataBuffer>() {
            @Override
            public void listen(DataBuffer data) {

            }
        });
        inputStreamReader.read();
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