package com.codejune.http;

import com.codejune.common.Builder;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.RegexUtil;
import com.codejune.common.util.StringUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpResponseResult
 *
 * @author ZJ
 * */
public final class HttpResponseResult<T> implements Builder {

    private boolean flag;

    private int code;

    private Map<String, String> header = new HashMap<>();

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

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    /**
     * 添加header
     *
     * @param key key
     * @param value value
     * */
    public void addHeader(String key, String value) {
        this.header.put(key, value);
    }

    /**
     * 获取下载文件名
     *
     * @return 下载的文件名
     * */
    public String getDownloadFileName() {
        String contentDisposition = this.header.get("Content-Disposition");
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

    @Override
    public void build(Object object) {
        HttpResponseResult<?> parse = ObjectUtil.transform(object, HttpResponseResult.class);
        this.setCode(parse.getCode());
        this.setHeader(parse.getHeader());
    }

}