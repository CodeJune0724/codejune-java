package com.codejune.common;

/**
 * 字符集
 *
 * @author ZJ
 * */
public enum Charset {

    UTF_8("UTF-8"),

    GBK("GBK");

    private final String name;

    Charset(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 获取实体
     *
     * @param charset charset
     *
     * @return Charset
     * */
    public static Charset getInstance(String charset) {
        Charset[] values = Charset.values();
        for (Charset charset1 : values) {
            if (charset1.getName().equals(charset)) {
                return charset1;
            }
        }
        return null;
    }

}