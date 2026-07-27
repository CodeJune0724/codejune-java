package com.codejune.http;

import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求
 *
 * @author ZJ
 * */
public final class HttpRequest {

    private final String url;

    private final Type type;

    private ContentType contentType;

    private final List<Header> header = new ArrayList<>();

    private Object body;

    private int timeout = -1;

    private boolean timeoutResend = false;

    private Proxy proxy;

    private boolean redirect = false;

    public HttpRequest(String url, Type type) {
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

    public HttpRequest setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public List<Header> getHeader() {
        return header;
    }

    public Object getBody() {
        return body;
    }

    public HttpRequest setBody(Object body) {
        this.body = body;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public HttpRequest setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isTimeoutResend() {
        return timeoutResend;
    }

    public HttpRequest setTimeoutResend(boolean timeoutResend) {
        this.timeoutResend = timeoutResend;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public HttpRequest setProxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }

    public boolean isRedirect() {
        return this.redirect;
    }

    public HttpRequest setRedirect(boolean redirect) {
        this.redirect = redirect;
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
    public HttpRequest addHeader(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        this.header.add(new Header(key, value));
        return this;
    }

    /**
     * 移除请求头
     *
     * @param key key
     *
     * @return this
     * */
    public HttpRequest deleteHeader(String key) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        List<Header> newHeaderList = ArrayUtil.filter(this.header, header -> !key.equalsIgnoreCase(header.getKey()));
        this.header.clear();
        this.header.addAll(newHeaderList);
        return this;
    }

}