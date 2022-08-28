package com.codejune.common.io.reader;

import com.codejune.common.Progress;
import com.codejune.common.Range;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.AbstractReader;
import com.codejune.common.listener.InputStreamReadListener;
import com.codejune.common.listener.ProgressListener;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * 文件读取器
 *
 * @author ZJ
 * */
public final class FileReader extends AbstractReader {

    private final File file;

    private int readSize = 1024;

    public FileReader(File file) {
        if (file == null) {
            throw new InfoException("file is null");
        }
        if (!file.isFile() || !file.exists()) {
            throw new InfoException("file不存在");
        }
        this.file = file;
    }

    public void setReadSize(int readSize) {
        this.readSize = readSize;
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * @param inputStreamReadListener inputStreamReadListener
     * @param progressListener progressListener
     * */
    public void read(Range range, InputStreamReadListener inputStreamReadListener, ProgressListener progressListener) {
        if (range == null) {
            range = new Range(0, null);
        }
        Integer difference = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (difference != null && difference == 0) {
            return;
        }
        if (inputStreamReadListener == null) {
            inputStreamReadListener = (bytes, size) -> {};
        }
        if (progressListener == null) {
            progressListener = data -> {};
        }
        RandomAccessFile randomAccessFile = null;
        final ProgressListener finalProgressListener = progressListener;
        try {
            randomAccessFile = new RandomAccessFile(this.file, "r");
            Progress progress = new Progress(difference == null ? randomAccessFile.length() : difference) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
            randomAccessFile.seek(range.getStart());
            byte[] bytes = new byte[this.readSize];
            int size = randomAccessFile.read(bytes);
            while (size != -1) {
                inputStreamReadListener.listen(bytes, size);
                progress.add(size);
                if (range.getEnd() != null && randomAccessFile.getFilePointer() >= range.getEnd()) {
                    break;
                }
                size = randomAccessFile.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * @param inputStreamReadListener inputStreamReadListener
     * */
    public void read(Range range, InputStreamReadListener inputStreamReadListener) {
        read(range, inputStreamReadListener, null);
    }

    /**
     * 读取
     *
     * @param inputStreamReadListener inputStreamReadListener
     * */
    public void read(InputStreamReadListener inputStreamReadListener) {
        read(null, inputStreamReadListener);
    }

}