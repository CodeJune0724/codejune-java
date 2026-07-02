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
        String id;
        String fileName;
        if (url.startsWith("https")) {
            throw new BaseException("未实现");
        } else {
            String[] split = url.split("/");
            id = split[0];
            fileName = split[1];
        }
        return new Http("https://silver.yukaidi.com/api/v4/file/url", Type.POST)
                .setContentType(ContentType.APPLICATION_JSON)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
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