package com.codejune.http;

import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.Builder;
import com.codejune.core.ClassInfo;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HttpResponse
 *
 * @author ZJ
 * */
public final class HttpResponse<T> implements Builder {

    private boolean flag;

    private int code;

    private final List<Header> headerList = new ArrayList<>();

    private T body;

    private HttpRequest httpRequest;

    public boolean isFlag() {
        return this.flag;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void build(Object object) {
        if (object instanceof HttpResponse<?> other) {
            this.code = other.code;
            this.flag = other.flag;
            this.headerList.clear();
            this.headerList.addAll(other.headerList);
            this.body = (T) other.body;
            this.httpRequest = other.httpRequest;
            return;
        }
        if (object != null) {
            ObjectUtil.assignment(this, object);
        }
    }

    /**
     * 设置状态码
     *
     * <p>2xx 视为成功。</p>
     *
     * @param code 状态码
     * */
    public void setCode(int code) {
        this.code = code;
        this.flag = code >= 200 && code < 300;
    }

    /**
     * 获取响应头
     *
     * @return 响应头
     * */
    public List<Header> getHeaderList() {
        List<Header> result = new ArrayList<>(this.headerList.size());
        for (Header header : this.headerList) {
            result.add(new Header(header.getKey(), header.getValue()));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 获取header
     *
     * @param key key
     *
     * @return List<Header>
     * */
    public List<Header> getHeaderList(String key) {
        List<Header> result = new ArrayList<>();
        if (StringUtil.isEmpty(key)) {
            return result;
        }
        for (Header item : this.headerList) {
            if (item.getKey().equalsIgnoreCase(key)) {
                result.add(new Header(item.getKey(), item.getValue()));
            }
        }
        return result;
    }

    /**
     * 获取header
     *
     * @param key key
     *
     * @return Header
     * */
    public Header getHeader(String key) {
        List<Header> headerList = this.getHeaderList(key);
        if (!ObjectUtil.isEmpty(headerList)) {
            return headerList.getFirst();
        }
        return null;
    }

    /**
     * 添加header
     *
     * @param key key
     * @param value value
     * */
    public void addHeader(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return;
        }
        this.headerList.add(new Header(key, value));
    }

    public T getBody() {
        return this.body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    /**
     * getHttpRequest
     *
     * @return httpRequest
     * */
    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * setHttpRequest
     *
     * @param httpRequest httpRequest
     * */
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * 获取下载文件名
     *
     * @return 下载的文件名
     * */
    public String getDownloadFileName() {
        Header header = this.getHeader("Content-Disposition");
        if (header != null && !StringUtil.isEmpty(header.getValue())) {
            String contentDisposition = header.getValue();
            String fileName = this.parseFileName(contentDisposition);
            if (!StringUtil.isEmpty(fileName)) {
                return fileName;
            }
        }
        return this.getUrlFileName();
    }

    /**
     * 获取长度
     *
     * @return 长度
     * */
    public Long getContentLength() {
        Header header = this.getHeader("Content-Length");
        if (header == null || StringUtil.isEmpty(header.getValue())) {
            return null;
        }
        try {
            return Long.valueOf(header.getValue().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 转换
     *
     * @param tClass tClass
     * @param <E> T
     * */
    public <E> HttpResponse<E> parse(Class<E> tClass) {
        if (tClass == null) {
            throw new BaseException("class is null");
        }
        HttpResponse<E> result = new HttpResponse<>();
        result.build(this);
        if (this.body == null) {
            result.setBody(null);
            return result;
        }
        ClassInfo classInfo = new ClassInfo(tClass);
        if (classInfo.isInstanceof(Map.class) || classInfo.isInstanceof(Collection.class)) {
            result.setBody(Json.parse(this.body, tClass));
        } else {
            result.setBody(ObjectUtil.parse(this.body, tClass));
        }
        return result;
    }

    private String parseFileName(String contentDisposition) {
        Matcher starMatcher = Pattern.compile("filename\\*\\s*=\\s*([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentDisposition);
        if (starMatcher.find()) {
            String value = starMatcher.group(1).trim();
            int index = value.indexOf("''");
            if (index >= 0) {
                value = value.substring(index + 2);
            }
            String fileName = sanitizeFileName(percentDecode(value));
            if (!StringUtil.isEmpty(fileName)) {
                return fileName;
            }
        }
        Matcher matcher = Pattern.compile("filename\\s*=\\s*(?:\"([^\"]*)\"|([^;]*))", Pattern.CASE_INSENSITIVE).matcher(contentDisposition);
        if (matcher.find()) {
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            return sanitizeFileName(percentDecode(value));
        }
        return null;
    }

    private String getUrlFileName() {
        if (this.httpRequest == null || StringUtil.isEmpty(this.httpRequest.getUrl())) {
            return "";
        }
        String url = this.httpRequest.getUrl();
        int index = url.indexOf('?');
        if (index >= 0) {
            url = url.substring(0, index);
        }
        index = url.indexOf('#');
        if (index >= 0) {
            url = url.substring(0, index);
        }
        index = url.lastIndexOf('/');
        String fileName = index >= 0 ? url.substring(index + 1) : url;
        String result = sanitizeFileName(percentDecode(fileName));
        return result == null ? "" : result;
    }

    private static String percentDecode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        String result = fileName
                .replace('\r', '_')
                .replace('\n', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .trim();
        if (result.isEmpty() || ".".equals(result) || "..".equals(result)) {
            return null;
        }
        return result;
    }

}