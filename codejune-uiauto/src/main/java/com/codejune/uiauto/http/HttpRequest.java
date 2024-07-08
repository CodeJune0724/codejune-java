package com.codejune.uiauto.http;

import java.util.Map;

/**
 * HttpRequest
 *
 * @author ZJ
 * */
public final class HttpRequest {

    private final String url;

    private final String type;

    private final Map<String, String> header;

    private final String body;

    public HttpRequest(String url, String type, Map<String, String> header, String body) {
        this.url = url;
        this.type = type;
        this.header = header;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

}