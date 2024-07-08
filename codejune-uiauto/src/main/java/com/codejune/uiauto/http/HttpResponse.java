package com.codejune.uiauto.http;

import java.util.Map;

/**
 * HttpResponse
 *
 * @author ZJ
 * */
public final class HttpResponse {

    private final int status;

    private final Map<String, String> header;

    private final String body;

    public HttpResponse(int status, Map<String, String> header, String body) {
        this.status = status;
        this.header = header;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

}