package com.codejune.common.io.reader;

import com.codejune.common.Progress;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.AbstractReader;
import com.codejune.common.listener.InputStreamReadListener;
import com.codejune.common.listener.ProgressListener;
import java.io.InputStream;

/**
 * 输入流读取器
 *
 * @author ZJ
 * */
public final class InputStreamReader extends AbstractReader {

    private final InputStream inputStream;

    private int readSize = 1024;

    public InputStreamReader(InputStream inputStream) {
        if (inputStream == null) {
            throw new InfoException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    public void setReadSize(int readSize) {
        this.readSize = readSize;
    }

    /**
     * 读取
     *
     * @param inputStreamReadListener inputStreamReadListener
     * @param progressListener progressListener
     * */
    public void read(InputStreamReadListener inputStreamReadListener, ProgressListener progressListener) {
        if (inputStreamReadListener == null) {
            inputStreamReadListener = (bytes, size) -> {};
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
                inputStreamReadListener.listen(bytes, size);
                progress.add(size);
                size = this.inputStream.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 读取
     *
     * @param inputStreamReadListener inputStreamReadListener
     * */
    public void read(InputStreamReadListener inputStreamReadListener) {
        read(inputStreamReadListener, null);
    }

}