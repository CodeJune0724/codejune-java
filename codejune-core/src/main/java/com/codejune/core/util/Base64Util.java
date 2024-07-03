package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64Util
 *
 * @author ZJ
 * */
public final class Base64Util {

    /**
     * base64编码
     *
     * @param data data
     *
     * @return String
     */
    public static String encode(String data) {
        if (StringUtil.isEmpty(data)) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * base64编码
     *
     * @param bytes bytes
     *
     * @return String
     * */
    public static String encode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base64编码
     *
     * @param file 文件
     *
     * @return String
     * */
    public static String encode(File file) {
        try (InputStream inputStream = IOUtil.getInputStream(file)) {
            byte[] bytes = new byte[inputStream.available()];
            if (inputStream.read(bytes) > 0) {
                return encode(bytes);
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * base64解码
     *
     * @param data data
     *
     * @return String
     */
    public static String decode(String data) {
        if (StringUtil.isEmpty(data)) {
            return null;
        }
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    /**
     * base64解码
     *
     * @param data data
     *
     * @return byte[]
     * */
    public static byte[] decodeToByte(String data) {
        if (StringUtil.isEmpty(data)) {
            return null;
        }
        return Base64.getDecoder().decode(data);
    }

}