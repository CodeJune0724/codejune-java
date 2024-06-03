package com.codejune;

import com.codejune.common.BaseException;
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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * http组件
 *
 * @author ZJ
 */
public final class Http {

    private final String url;

    private final Type type;

    private ContentType contentType;

    private final List<Header> headerList = new ArrayList<>();

    private Object body;

    private int timeout = -1;

    private Function<HttpResponseResult<String>, Boolean> resend = stringHttpResponseResult -> false;

    {
        addHeader("accept", "*/*");
        addHeader("connection", "Keep-Alive");
    }

    public Http(String url, Type type) {
        if (StringUtil.isEmpty(url)) {
            throw new BaseException("url is null");
        }
        if (type == null) {
            throw new BaseException("type is null");
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

    public ContentType getContentType() {
        return contentType;
    }

    public Http setContentType(ContentType contentType) {
        this.contentType = contentType;
        String key = "Content-type";
        if (this.contentType == null) {
            headerList.removeIf(header -> key.equals(header.getKey()));
        } else {
            if (contentType != ContentType.FORM_DATA) {
                addHeader(key, contentType.getContentType());
            }
        }
        return this;
    }

    public Object getBody() {
        return body;
    }

    public Http setBody(Object body) {
        this.body = body;
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
    public Http addHeader(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return this;
        }
        if (key.equalsIgnoreCase("content-length")) {
            return this;
        }
        this.headerList.add(new Header(key, value));
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param timeout timeout
     *
     * @return this
     * */
    public Http setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置重连补偿
     *
     * @param resend resend
     *
     * @return this
     * */
    public Http resend(Function<HttpResponseResult<String>, Boolean> resend) {
        if (resend == null) {
            return this;
        }
        this.resend = resend;
        return this;
    }

    /**
     * 发送
     *
     * @param listener listener
     * */
    public void send(Consumer<HttpResponseResult<InputStream>> listener) {
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
            httpEntityEnclosingRequestBase.setConfig(RequestConfig.custom().setConnectTimeout(this.timeout).setSocketTimeout(this.timeout).setConnectionRequestTimeout(this.timeout).build());
            for (Header header : headerList) {
                httpEntityEnclosingRequestBase.setHeader(header.getKey(), header.getValue());
            }
            if (body != null) {
                if (contentType == ContentType.APPLICATION_JSON) {
                    httpEntity = new StringEntity(Json.toString(body), "UTF-8");
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
                if (listener == null) {
                    listener = data -> {};
                }
                listener.accept(httpResponseResult);
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
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
        });
        if (ObjectUtil.equals(true, resend.apply(result))) {
            return send();
        }
        return result;
    }

}