package com.etl.api.util;

import cn.dev33.satoken.secure.SaSecureUtil;

public final class AESUtil {

    /**
     * 加密密钥
     */
    private static final String AES_KEY = "rKL@x*BAnu";

    private AESUtil() {
    }

    public static String encrypt(String text) {
        return SaSecureUtil.aesEncrypt(AES_KEY, text);
    }

    public static String decrypt(String text) {
        return SaSecureUtil.aesDecrypt(AES_KEY, text);
    }
}
