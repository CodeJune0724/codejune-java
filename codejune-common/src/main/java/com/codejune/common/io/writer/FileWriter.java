package com.codejune.common.io.writer;

import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.Writer;
import com.codejune.common.io.reader.InputStreamReader;
import com.codejune.common.util.IOUtil;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * FileWriter
 *
 * @author ZJ
 * */
public final class FileWriter extends Writer implements Closeable {

    private final File file;

    public FileWriter(File file) {
        super(IOUtil.getOutputStream(file));
        this.file = file;
    }

    @Override
    public void close() {
        IOUtil.close(outputStream);
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * @param position 指定位置
     * */
    public void write(InputStream inputStream, long position) {
        if (inputStream == null) {
            return;
        }
        if (position < 0) {
            throw new InfoException("position < 0");
        }
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(this.file, "w");
            randomAccessFile.seek(position);
            final RandomAccessFile finalRandomAccessFile = randomAccessFile;
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            inputStreamReader.setReadListener(data -> {
                try {
                    finalRandomAccessFile.write(data.getBytes(), 0, data.getLength());
                } catch (Exception e) {
                    throw new InfoException(e);
                }
                writeListener.listen(data);
            });
            inputStreamReader.read();
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            IOUtil.close(randomAccessFile);
        }
    }

}
