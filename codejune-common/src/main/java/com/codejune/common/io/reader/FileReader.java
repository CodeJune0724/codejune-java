package com.codejune.common.io.reader;

import com.codejune.common.Range;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.DataBuffer;
import com.codejune.common.util.IOUtil;
import java.io.RandomAccessFile;

/**
 * 文件读取器
 *
 * @author ZJ
 * */
public final class FileReader extends InputStreamReader {

    private final java.io.File file;

    public FileReader(java.io.File file) {
        super(IOUtil.getInputStream(file));
        this.file = file;
    }

    @Override
    public void read() {
        try {
            super.read();
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * */
    public void read(Range range) {
        if (range == null) {
            range = new Range(0, null);
        }
        Integer difference = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (difference != null && difference == 0) {
            return;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(this.file, "r");
            randomAccessFile.seek(range.getStart());
            byte[] bytes = new byte[this.readSize];
            int size = randomAccessFile.read(bytes);
            while (size != -1) {
                readListener.listen(new DataBuffer(bytes, size));
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

}