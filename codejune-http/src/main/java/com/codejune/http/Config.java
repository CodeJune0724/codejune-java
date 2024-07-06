package com.codejune.http;

import com.codejune.core.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 配置
 *
 * @author ZJ
 * */
public final class Config {

    private final String url;

    private final Type type;

    private ContentType contentType;

    private final List<Header> header = new ArrayList<>();

    private Object body;

    private int timeout = -1;

    private Function<HttpResponseResult<String>, Boolean> resend = null;

    private boolean timeoutResend = false;

    public Config(String url, Type type) {
        this.url = url;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Config setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public List<Header> getHeader() {
        return header;
    }

    public Object getBody() {
        return body;
    }

    public Config setBody(Object body) {
        this.body = body;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Config setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Function<HttpResponseResult<String>, Boolean> getResend() {
        return resend;
    }

    public Config setResend(Function<HttpResponseResult<String>, Boolean> resend) {
        this.resend = resend;
        return this;
    }

    public boolean isTimeoutResend() {
        return timeoutResend;
    }

    public Config setTimeoutResend(boolean timeoutResend) {
        this.timeoutResend = timeoutResend;
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key 键
     * @param value 值
     *
     * @return this
     * */
    public Config addHeader(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        if (key.equalsIgnoreCase("content-length")) {
            return this;
        }
        this.header.add(new Header(key, value));
        return this;
    }

}