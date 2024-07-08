package com.codejune.uiauto.http;

import java.util.Map;

/**
 * HttpRequest
 *
 * @author ZJ
 * */
public final class HttpRequest {

    private String url;

    private String type;

    private Map<String, String> header;

    private String body;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}