package com.codejune.common.io.writer;

import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.Writer;
import com.codejune.common.io.reader.InputStreamReader;
import com.codejune.common.util.IOUtil;
import java.io.ByteArrayInputStream;
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

    public FileWriter(File file, boolean append) {
        super(IOUtil.getOutputStream(file, append));
        this.file = file;
    }

    public FileWriter(File file) {
        this(file, false);
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
            randomAccessFile = new RandomAccessFile(this.file, "rw");
            randomAccessFile.seek(position);
            final RandomAccessFile finalRandomAccessFile = randomAccessFile;
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            inputStreamReader.setListener(data -> {
                try {
                    finalRandomAccessFile.write(data.array(), 0, data.limit());
                } catch (Exception e) {
                    throw new InfoException(e);
                }
                listen.then(data);
            });
            inputStreamReader.read();
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            IOUtil.close(randomAccessFile);
        }
    }

    /**
     * 写入
     *
     * @param bytes bytes
     * @param position 指定位置
     * */
    public void write(byte[] bytes, long position) {
        if (bytes == null) {
            return;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            write(byteArrayInputStream, position);
        } finally {
            IOUtil.close(byteArrayInputStream);
        }
    }

}