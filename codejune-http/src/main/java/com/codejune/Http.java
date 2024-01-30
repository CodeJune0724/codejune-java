package com.codejune;

import com.codejune.common.Action;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.*;
import com.codejune.http.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * http组件
 *
 * @author ZJ
 */
public final class Http {

    private final String url;

    private final Type type;

    private final List<Header> headerList = new ArrayList<>();

    private ContentType contentType;

    private Object body;

    {
        addHeader("accept", "*/*");
        addHeader("connection", "Keep-Alive");
    }

    public Http(String url, Type type) {
        if (StringUtil.isEmpty(url)) {
            throw new InfoException("url is null");
        }
        if (type == null) {
            throw new InfoException("type is null");
        }
        this.url = url;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    public List<Header> getHeaderList() {
        return headerList;
    }

    /**
     * 添加请求头
     *
     * @param key 键
     * @param value 值
     * */
    public void addHeader(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return;
        }
        if (key.equalsIgnoreCase("content-length")) {
            return;
        }
        this.headerList.add(new Header(key, value));
    }

    /**
     * 删除请求头
     *
     * @param key key
     * */
    public void deleteHeader(String key) {
        if (StringUtil.isEmpty(key)) {
            return;
        }
        getHeaderList().removeIf(header -> key.equals(header.getKey()));
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
        String key = "Content-type";
        if (this.contentType == null) {
            headerList.removeIf(header -> key.equals(header.getKey()));
        } else {
            if (contentType != ContentType.FORM_DATA) {
                addHeader(key, contentType.getContentType());
            }
        }
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * 发送
     *
     * @param action action
     * */
    public void send(Action<HttpResponseResult<InputStream>, ?> action) {
        HttpResponseResult<InputStream> httpResponseResult = new HttpResponseResult<>();
        HttpEntity httpEntity = null;
        try (CloseableHttpClient closeableHttpClient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build(), NoopHostnameVerifier.INSTANCE)).build()) {
            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return type.name();
                }
            };
            httpEntityEnclosingRequestBase.setURI(URI.create(url));
            httpEntityEnclosingRequestBase.setConfig(RequestConfig.custom().build());
            for (Header header : headerList) {
                httpEntityEnclosingRequestBase.setHeader(header.getKey(), header.getValue());
            }
            if (body != null) {
                if (contentType == ContentType.APPLICATION_JSON) {
                    httpEntity = new StringEntity(JsonUtil.toJsonString(body), "UTF-8");
                } else if (contentType == ContentType.FORM_DATA) {
                    MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
                    Map<String, Object> mapBody = MapUtil.parse(body, String.class, Object.class);
                    if (mapBody != null) {
                        for (String key : mapBody.keySet()) {
                            Object value = mapBody.get(key);
                            if (value == null) {
                                continue;
                            }
                            if (value instanceof File) {
                                FileBody fileBody = new FileBody((File) value);
                                multipartEntityBuilder = multipartEntityBuilder.addPart(key, fileBody);
                            } else {
                                multipartEntityBuilder.addTextBody(key, ObjectUtil.toString(value), org.apache.http.entity.ContentType.create("text/plain", StandardCharsets.UTF_8));
                            }
                        }
                    }
                    httpEntity = multipartEntityBuilder.build();
                } else if (contentType == ContentType.FORM_URLENCODED) {
                    if (body instanceof Map<?,?> map) {
                        Map<String, Object> stringObjectMap = MapUtil.parse(map, String.class, Object.class);
                        if (stringObjectMap == null) {
                            stringObjectMap = new HashMap<>();
                        }
                        String body = ArrayUtil.toString(stringObjectMap.keySet(), key -> {
                            Object value = map.get(key);
                            if (value == null) {
                                return null;
                            }
                            return key + "=" + ObjectUtil.toString(value);
                        }, "&");
                        httpEntity = new StringEntity(body == null ? "" : body, "UTF-8");
                    } else {
                        httpEntity = new StringEntity(ObjectUtil.toString(body), "UTF-8");
                    }
                } else {
                    httpEntity = new StringEntity(ObjectUtil.toString(body), "UTF-8");
                }
            }
            httpEntityEnclosingRequestBase.setEntity(httpEntity);
            try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpEntityEnclosingRequestBase)) {
                httpResponseResult.setCode(closeableHttpResponse.getStatusLine().getStatusCode());
                for (org.apache.http.Header header : closeableHttpResponse.getAllHeaders()) {
                    httpResponseResult.addHeader(header.getName(), header.getValue());
                }
                httpResponseResult.setBody(closeableHttpResponse.getEntity().getContent());
                if (action == null) {
                    action = (Action<HttpResponseResult<InputStream>, Object>) inputStreamHttpResponseResult -> null;
                }
                action.then(httpResponseResult);
            }
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(httpResponseResult.getBody());
            try {
                EntityUtils.consume(httpEntity);
            } catch (Exception ignored) {}
        }
    }

    /**
     * 发送
     *
     * @return HttpResponseResult
     * */
    public HttpResponseResult<String> send() {
        HttpResponseResult<String> result = new HttpResponseResult<>();
        send(httpResponseResult -> {
            result.build(httpResponseResult);
            result.setBody(new TextInputStreamReader(httpResponseResult.getBody()).getData());
            return null;
        });
        return result;
    }

}