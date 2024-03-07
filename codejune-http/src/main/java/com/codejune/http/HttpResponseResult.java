package com.codejune.http;

import com.codejune.common.Builder;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.JsonUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.RegexUtil;
import com.codejune.common.util.StringUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpResponseResult
 *
 * @author ZJ
 * */
public final class HttpResponseResult<T> implements Builder {

    private boolean flag;

    private int code;

    private final List<Header> headerList = new ArrayList<>();

    private T body;

    public boolean isFlag() {
        return flag;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
        this.flag = code == 200;
    }

    public List<Header> getHeaderList() {
        return headerList;
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

    /**
     * 获取header
     *
     * @return List<Header>
     * */
    public List<Header> getHeaderList(String key) {
        List<Header> result = new ArrayList<>();
        for (Header item : getHeaderList()) {
            if (item.getKey().equals(key)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 获取header
     *
     * @return Header
     * */
    public Header getHeader(String key) {
        List<Header> headerList = getHeaderList(key);
        if (ObjectUtil.isEmpty(headerList)) {
            return headerList.get(0);
        }
        return null;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    /**
     * 获取下载文件名
     *
     * @return 下载的文件名
     * */
    public String getDownloadFileName() {
        Header header = getHeader("Content-Disposition");
        String contentDisposition = null;
        if (header != null) {
            contentDisposition = header.getValue();
        }
        String result = null;
        if (!StringUtil.isEmpty(contentDisposition)) {
            result = RegexUtil.find("filename=(.*?)$", contentDisposition, 1);
            if (!StringUtil.isEmpty(result)) {
                try {
                    result = URLDecoder.decode(result, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new InfoException(e);
                }
            }
        }
        return result;
    }

    /**
     * 转换
     *
     * @param tClass tClass
     * @param <E> T
     * */
    public <E> HttpResponseResult<E> parse(Class<E> tClass) {
        HttpResponseResult<E> result = new HttpResponseResult<>();
        result.build(this);
        result.setBody(JsonUtil.parse(this.getBody(), tClass));
        return result;
    }

    @Override
    public void build(Object object) {
        ObjectUtil.assignment(this, ObjectUtil.transform(object, HttpResponseResult.class));
    }

}