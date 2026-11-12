package com.codejune.http;

import com.codejune.core.BaseException;
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

    private int connectTimeout = DEFAULT_TIMEOUT;

    private int readTimeout = DEFAULT_TIMEOUT;

    private boolean timeoutResend = false;

    private Proxy proxy;

    private boolean redirect = false;

    private boolean trustAllCertificate = false;

    private static final int DEFAULT_TIMEOUT = 30_000;

    public HttpRequest(String url, Type type) {
        if (StringUtil.isEmpty(url)) {
            throw new BaseException("url is empty");
        }
        if (type == null) {
            throw new BaseException("type is null");
        }
        this.url = url;
        this.type = type;
    }

    public String getUrl() {
        return this.url;
    }

    public Type getType() {
        return this.type;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public HttpRequest setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public List<Header> getHeader() {
        return this.header;
    }

    /**
     * 获取请求头
     *
     * @param key 键
     *
     * @return Header
     * */
    public Header getHeader(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        for (Header header : this.header) {
            if (key.equalsIgnoreCase(header.getKey())) {
                return header;
            }
        }
        return null;
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
        checkHeader(key, value);
        this.header.removeIf(header -> key.equalsIgnoreCase(header.getKey()));
        this.header.add(new Header(key, value));
        return this;
    }

    /**
     * 添加请求头，已存在同名请求头时不覆盖
     *
     * @param key 键
     * @param value 值
     *
     * @return this
     * */
    public HttpRequest addHeaderIfAbsent(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        if (this.getHeader(key) != null) {
            return this;
        }
        return this.addHeader(key, value);
    }

    /**
     * 添加User-Agent
     *
     * @return this
     * */
    public HttpRequest addUserAgent() {
        this.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");
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
        this.header.removeIf(header -> key.equalsIgnoreCase(header.getKey()));
        return this;
    }

    public Object getBody() {
        return this.body;
    }

    public HttpRequest setBody(Object body) {
        this.body = body;
        return this;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public HttpRequest setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public HttpRequest setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public boolean isTimeoutResend() {
        return this.timeoutResend;
    }

    public HttpRequest setTimeoutResend(boolean timeoutResend) {
        this.timeoutResend = timeoutResend;
        return this;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public HttpRequest setProxy(String host, int port) {
        if (StringUtil.isEmpty(host)) {
            throw new BaseException("proxy host is empty");
        }
        InetSocketAddress address = new InetSocketAddress(host, port);
        if (address.isUnresolved()) {
            throw new BaseException("proxy host 无法解析: " + host);
        }
        this.proxy = new Proxy(Proxy.Type.HTTP, address);
        return this;
    }

    public boolean isRedirect() {
        return this.redirect;
    }

    public HttpRequest setRedirect(boolean redirect) {
        this.redirect = redirect;
        return this;
    }

    public boolean isTrustAllCertificate() {
        return this.trustAllCertificate;
    }

    public HttpRequest setTrustAllCertificate(boolean trustAllCertificate) {
        this.trustAllCertificate = trustAllCertificate;
        return this;
    }

    private static void checkHeader(String key, String value) {
        if (containsLineBreak(key) || containsLineBreak(value)) {
            throw new BaseException("请求头不能包含换行符");
        }
    }

    private static boolean containsLineBreak(String value) {
        return value != null && (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0);
    }

}