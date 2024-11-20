package com.codejune.core.util;

import com.codejune.core.BaseException;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RsaUtil
 *
 * @author ZJ
 * */
public final class RsaUtil {

    /**
     * 使用公钥加密
     *
     * @param publicKey 公钥
     * @param data 数据
     *
     * @return 加密后的数据
     * */
    public static String encode(String publicKey, String data) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey p = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, p);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}