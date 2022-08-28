package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import java.io.*;

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
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
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
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
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
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
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
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

}