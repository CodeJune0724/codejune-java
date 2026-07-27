package com.codejune.tool.cloudstoragedownload;

import com.codejune.Http;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.http.ContentType;
import com.codejune.http.Type;
import com.codejune.tool.CloudStorageDownload;

/**
 * Yukaidi
 *
 * @author ZJ
 * */
public final class YukaidiCloudStorageDownload extends CloudStorageDownload {

    @Override
    public String getDirectUrl(String url) {
        Exception error = null;
        for (int i = 0; i < 5; i++) {
            try {
                return this.baseGetDirectUrl(url);
            } catch (Exception e) {
                error = e;
            }
        }
        throw new BaseException(error);
    }

    private String baseGetDirectUrl(String url) {
        if (url.startsWith("https://silver.yukaidi.com/f")) {
            return url;
        }
        String[] split = url.split("/");
        if (split.length != 2) {
            throw new BaseException("未实现");
        }
        String id = split[0];
        String fileName = split[1];
        return new Http("https://silver.yukaidi.com/api/v4/file/url", Type.POST)
                .setContentType(ContentType.APPLICATION_JSON)
                .addUserAgent()
                .setBody("{\n" +
                        "    \"uris\": [\n" +
                        "        \"cloudreve://" + id + "@share/" + fileName + "\"\n" +
                        "    ],\n" +
                        "    \"download\": true\n" +
                        "}")
                .send()
                .parse(Json.class)
                .getBody()
                .get("data").get("urls").get(0).get("url", String.class);
    }

}