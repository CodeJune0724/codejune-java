package com.codejune.core.util;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.InputStreamReader;
import java.io.*;
import java.nio.file.Files;

/**
 * IOUtil
 *
 * @author ZJ
 * */
public final class IOUtil {

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

    /**
     * 获取byte[]
     *
     * @param inputStream inputStream
     *
     * @return byte[]
     * */
    public static byte[] getByte(InputStream inputStream) {
        return new InputStreamReader(inputStream).getByte();
    }

}