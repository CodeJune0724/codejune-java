package com.codejune.common.handler;

import java.io.InputStream;

/**
 * DownloadHandler
 *
 * @author ZJ
 * */
public interface DownloadHandler {

    /**
     * 下载
     *
     * @param inputStream inputStream
     * */
    void download(InputStream inputStream);

}