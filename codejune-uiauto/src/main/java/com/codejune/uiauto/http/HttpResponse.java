package com.codejune.uiauto.http;

import java.util.Map;

/**
 * HttpResponse
 *
 * @author ZJ
 * */
public final class HttpResponse {

    private int status;

    private Map<String, String> header;

    private String body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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