package com.codejune.core.io.writer;

import com.codejune.core.BaseException;
import com.codejune.core.io.Reader;
import com.codejune.core.io.Writer;
import com.codejune.core.io.reader.InputStreamReader;
import com.codejune.core.util.IOUtil;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * FileWriter
 *
 * @author ZJ
 * */
public final class FileWriter extends Writer {

    private final File file;

    public FileWriter(File file) {
        super(IOUtil.getOutputStream(file));
        this.file = file;
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
        if (position < 0) {
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "rw")) {
            randomAccessFile.seek(position);
            try {
                randomAccessFile.write(bytes);
            } catch (Exception e) {
                throw new BaseException(e);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 写入
     *
     * @param byteBuffer byteBuffer
     * @param position 指定位置
     * */
    public void write(ByteBuffer byteBuffer, long position) {
        this.write(Reader.getByte(byteBuffer), position);
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
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "rw")) {
            randomAccessFile.seek(position);
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                inputStreamReader.setReadSize(this.getWriteSize());
                inputStreamReader.read(byteBuffer -> {
                    try {
                        randomAccessFile.write(Reader.getByte(byteBuffer));
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 追加写入
     *
     * @param bytes bytes
     * */
    public void writeAppend(byte[] bytes) {
        this.write(bytes, this.file.length());
    }

    /**
     * 追加写入
     *
     * @param byteBuffer byteBuffer
     * */
    public void writeAppend(ByteBuffer byteBuffer) {
        this.writeAppend(Reader.getByte(byteBuffer));
    }

    /**
     * 追加写入
     *
     * @param inputStream inputStream
     * */
    public void writeAppend(InputStream inputStream) {
        this.write(inputStream, this.file.length());
    }

}