package com.codejune.core;

import com.codejune.core.util.ObjectUtil;

/**
 * 基础异常
 *
 * @author ZJ
 * */
public final class BaseException extends RuntimeException {

    public BaseException(Object message) {
        super(ObjectUtil.toString(message));
    }

    public BaseException(Exception exception) {
        super(exception.getMessage());
    }

}