package com.codejune.core.util;

import com.codejune.core.BaseException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AESUtil
 *
 * @author ZJ
 * */
public final class AESUtil {

    /**
     * 加密
     *
     * @param value value
     * @param key key
     *
     * @return 加密后的数据
     * */
    public static String encode(String value, String key) {
        if (StringUtil.isEmpty(value) || StringUtil.isEmpty(key)) {
            throw new BaseException("param is empty");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"));
            return Base64Util.encode(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 解密
     *
     * @param value value
     * @param key key
     *
     * @return 解密后的数据
     * */
    public static String decode(String value, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(value);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}