package com.codejune.common.exception;

import com.codejune.common.util.ObjectUtil;

/**
 * 信息异常
 *
 * @author ZJ
 * */
public final class InfoException extends RuntimeException {

    public InfoException(Object message) {
        super(ObjectUtil.toString(message));
    }

    public InfoException(Exception exception) {
        super(exception.getMessage());
    }

}