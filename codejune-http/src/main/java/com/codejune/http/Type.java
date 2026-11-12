package com.codejune.http;

/**
 * 请求类型
 *
 * @author ZJ
 * */
public enum Type {

    GET,

    POST,

    PUT,

    DELETE,

    HEAD,

    OPTIONS,

    PATCH;

    /**
     * 是否是幂等方法
     *
     * @return boolean
     * */
    public boolean isIdempotent() {
        return switch (this) {
            case GET, HEAD, PUT, DELETE, OPTIONS -> true;
            case POST, PATCH -> false;
        };
    }

}