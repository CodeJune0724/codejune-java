package com.codejune.common.exception;

/**
 * 错误异常
 *
 * @author ZJ
 * */
public final class ErrorException extends Error {

    public ErrorException(String message) {
        super(message);
    }

}