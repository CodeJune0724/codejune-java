package com.codejune.common;

/**
 * Action
 *
 * @author ZJ
 * */
public interface Action<PARAMETER, RETURN> {

    /**
     * 执行
     *
     * @param parameter 参数
     *
     * @return 返回参数
     * */
    RETURN then(PARAMETER parameter);

    /**
     * 执行
     *
     * @return 返回参数
     * */
    default RETURN then() {
        return then(null);
    }

}