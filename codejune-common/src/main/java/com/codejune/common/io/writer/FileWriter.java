package com.codejune.common.io.writer;

import com.codejune.common.BaseException;
import com.codejune.common.io.reader.InputStreamReader;
import com.codejune.common.util.FileUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * FileWriter
 *
 * @author ZJ
 * */
public final class FileWriter {

    private final File file;

    public FileWriter(File file) {
        this.file = file;
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
            throw new BaseException("position < 0");
        }
        if (!FileUtil.isFile(this.file)) {
            throw new BaseException("not file");
        }
        if (!FileUtil.exist(this.file)) {
            new com.codejune.common.os.File(this.file);
        }
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "rw")) {
            randomAccessFile.seek(position);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            inputStreamReader.read((data -> {
                try {
                    randomAccessFile.write(data.array(), 0, data.limit());
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }));
        } catch (Exception e) {
            throw new BaseException(e);
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
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            write(byteArrayInputStream, position);
        } catch (Exception exception) {
            throw new BaseException(exception);
        }
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        this.write(inputStream, 0);
    }

    /**
     * 写入
     *
     * @param bytes bytes
     * */
    public void write(byte[] bytes) {
        this.write(bytes, 0);
    }

    /**
     * 写入
     *
     * @param inputStream inputStream
     * @param append 是否追加
     * */
    public void write(InputStream inputStream, boolean append) {
        this.write(inputStream, append ? new com.codejune.common.os.File(this.file).getSize() : 0);
    }

    /**
     * 写入
     *
     * @param bytes bytes
     * @param append 追加
     * */
    public void write(byte[] bytes, boolean append) {
        this.write(bytes, append ? new com.codejune.common.os.File(this.file).getSize() : 0);
    }

}