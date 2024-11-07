package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.security.MessageDigest;

/**
 * Md5Util
 *
 * @author ZJ
 * */
public final class Md5Util {

    /**
     * 加密
     *
     * @param data data
     *
     * @return md5
     * */
    public static String encode(String data) {
        if (data == null) {
            return null;
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        }catch (Exception e) {
            throw new BaseException(e);
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (byte item : messageDigest.digest(data.getBytes())) {
            stringBuffer.append(String.format("%02x", item));
        }
        return stringBuffer.toString();
    }

}