package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.io.File;
import java.io.IOException;
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
     * 编码
     *
     * @param s s
     *
     * @return 编码后的结果
     */
    public static String encode(String s) {
        Base64.Encoder encoder = Base64.getEncoder();
        String result;
        result = encoder.encodeToString(s.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    /**
     * 解码
     *
     * @param s s
     *
     * @return 解码后的结果
     */
    public static String decode(String s) {
        Base64.Decoder decoder = Base64.getDecoder();
        String result;
        result = new String(decoder.decode(s), StandardCharsets.UTF_8);
        return result;
    }

    /**
     * base64解码
     *
     * @param data 数据
     *
     * @return base64编码
     * */
    public static byte[] decodeToByte(String data) {
        if (StringUtil.isEmpty(data)) {
            return null;
        }
        return Base64.getDecoder().decode(data);
    }

    /**
     * 将文件转换成base64编码
     *
     * @param file 文件
     *
     * @return base64编码
     * */
    public static String encode(File file) {
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.getInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            int read = inputStream.read(bytes);
            if (read > 0) {
                return Base64.getEncoder().encodeToString(bytes);
            }
            throw new BaseException("转码失败");
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
        }
    }

}