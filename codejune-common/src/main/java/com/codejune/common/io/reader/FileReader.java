package com.codejune.common.io.reader;

import com.codejune.common.Closeable;
import com.codejune.common.Range;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.ObjectUtil;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * 文件读取器
 *
 * @author ZJ
 * */
public final class FileReader extends InputStreamReader implements Closeable {

    private final java.io.File file;

    public FileReader(java.io.File file) {
        super(IOUtil.getInputStream(file));
        this.file = file;
    }

    @Override
    public void close() {
        IOUtil.close(inputStream);
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * */
    public void read(Range range) {
        if (range == null) {
            range = new Range(0L, null);
        }
        Long length = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (length != null && length == 0) {
            return;
        }
        if (length != null) {
            this.setSize(ObjectUtil.transform(length, int.class));
        }
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(this.file, "r");
            randomAccessFile.seek(range.getStart());
            byte[] bytes = new byte[this.size];
            int size = randomAccessFile.read(bytes);
            while (size != -1) {
                listener.listen(ByteBuffer.wrap(bytes, 0, size));
                if (range.getEnd() != null && randomAccessFile.getFilePointer() >= range.getEnd()) {
                    break;
                }
                size = randomAccessFile.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            IOUtil.close(randomAccessFile);
        }
    }

}