package com.codejune.common.io.reader;

import com.codejune.common.Progress;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.AbstractReader;
import com.codejune.common.listener.BinaryReadListener;
import com.codejune.common.listener.ProgressListener;
import java.io.InputStream;

/**
 * 二进制读取器
 *
 * @author ZJ
 * */
public final class BinaryReader extends AbstractReader {

    private int readSize = 1024;

    public BinaryReader(InputStream inputStream) {
        super(inputStream);
    }

    public void setReadSize(int readSize) {
        this.readSize = readSize;
    }

    /**
     * 读字节
     *
     * @param binaryReadListener readListener
     * @param progressListener progressListener
     * */
    public void read(BinaryReadListener binaryReadListener, ProgressListener progressListener) {
        if (binaryReadListener == null) {
            binaryReadListener = (bytes, size) -> {};
        }
        if (progressListener == null) {
            progressListener = data -> {};
        }
        ProgressListener finalProgressListener = progressListener;
        try {
            Progress progress = new Progress(this.inputStream.available()) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
            byte[] bytes = new byte[this.readSize];
            int size = this.inputStream.read(bytes);
            while (size != -1) {
                binaryReadListener.listen(bytes, size);
                progress.add(size);
                size = this.inputStream.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}