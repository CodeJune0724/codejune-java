package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.io.*;
import java.nio.file.Files;

/**
 * IOUtil
 *
 * @author ZJ
 * */
public final class IOUtil {

    /**
     * 关闭inputStream
     *
     * @param inputStream inputStream
     * */
    public static void close(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {}
    }

    /**
     * 关闭reader
     *
     * @param reader reader
     * */
    public static void close(Reader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException ignored) {}
    }

    /**
     * 关闭outputStream
     *
     * @param outputStream outputStream
     * */
    public static void close(OutputStream outputStream) {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        } catch (IOException ignored) {}
    }

    /**
     * 关闭outputStream
     *
     * @param writer writer
     * */
    public static void close(Writer writer) {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException ignored) {}
    }

    /**
     * 关闭randomAccessFile
     *
     * @param randomAccessFile randomAccessFile
     * */
    public static void close(RandomAccessFile randomAccessFile) {
        if (randomAccessFile == null) {
            return;
        }
        try {
            randomAccessFile.close();
        } catch (IOException ignored) {}
    }

    /**
     * 获取输入流
     *
     * @param file file
     *
     * @return InputStream
     * */
    public static InputStream getInputStream(java.io.File file) {
        if (!FileUtil.exist(file)) {
            throw new BaseException("文件不存在");
        }
        if (!file.isFile()) {
            throw new BaseException("非文件");
        }
        try {
            return Files.newInputStream(file.toPath());
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取输入流
     *
     * @param data data
     * */
    public static InputStream getInputStream(String data) {
        if (data == null) {
            return null;
        }
        return getInputStream(data.getBytes());
    }

    /**
     * 获取输入流
     *
     * @param data data
     * */
    public static InputStream getInputStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    /**
     * 获取输出流
     *
     * @param file file
     * @param append 是否追加
     *
     * @return OutputStream
     * */
    public static OutputStream getOutputStream(java.io.File file, boolean append) {
        if (!FileUtil.exist(file)) {
            new com.codejune.core.os.File(file);
        }
        if (!FileUtil.isFile(file)) {
            throw new BaseException("非文件");
        }
        try {
            return new FileOutputStream(file, append);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取输出流
     *
     * @param file file
     *
     * @return OutputStream
     * */
    public static OutputStream getOutputStream(java.io.File file) {
        return getOutputStream(file, false);
    }

}