package com.codejune.http;

import java.io.InputStream;

/**
 * 发送结果处理
 *
 * @author ZJ
 * */
public interface HttpResponseResultHandler {

    /**
     * 处理
     *
     * @param httpResponseResult httpResponseResult
     * */
    void handler(HttpResponseResult<InputStream> httpResponseResult);

}