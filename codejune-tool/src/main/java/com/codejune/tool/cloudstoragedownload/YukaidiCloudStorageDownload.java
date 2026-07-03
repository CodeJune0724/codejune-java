package com.codejune.tool.cloudstoragedownload;

import com.codejune.Http;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.http.ContentType;
import com.codejune.http.Header;
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
        if (url.startsWith("https://silver.yukaidi.com/f")) {
            while (true) {
                Header locationHeader = new Http(url, Type.GET)
                        .addUserAgent()
                        .send()
                        .getHeader("Location");
                if (locationHeader == null) {
                    throw new BaseException("重定向地址获取失败");
                }
                url = locationHeader.getValue();
                if (url.startsWith("/")) {
                    url = "https://silver.yukaidi.com" + url;
                    continue;
                }
                if (url.startsWith("https://skip.yukaidi.top")) {
                    continue;
                }
                return url;
            }
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