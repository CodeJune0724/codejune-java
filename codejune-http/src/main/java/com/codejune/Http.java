package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.*;
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

    private Config config;

    private int timeoutResendNumber = 10;

    public Http(String url, Type type) {
        this.config = new Config(url, type);
        this.addHeader("accept", "*/*");
        this.addHeader("connection", "Keep-Alive");
    }

    public Config getConfig() {
        return this.config;
    }

    /**
     * 设置ContentType
     *
     * @param contentType contentType
     *
     * @return this
     * */
    public Http setContentType(ContentType contentType) {
        this.config.setContentType(contentType);
        String key = "Content-type";
        if (this.config.getContentType() == null) {
            this.config.getHeader().removeIf(header -> key.equals(header.getKey()));
        } else {
            if (contentType != ContentType.FORM_DATA) {
                addHeader(key, contentType.getContentType());
            }
        }
        return this;
    }

    /**
     * 设置body
     *
     * @param body body
     *
     * @return this
     * */
    public Http setBody(Object body) {
        this.config.setBody(body);
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
        this.config.addHeader(key, value);
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
        this.config.setTimeout(timeout);
        return this;
    }

    /**
     * 设置重连补偿
     *
     * @param resend resend
     *
     * @return this
     * */
    public Http setResend(Function<HttpResponseResult<String>, Boolean> resend) {
        this.config.setResend(resend);
        return this;
    }

    /**
     * 连接失败自动重连
     *
     * @param timeoutResend timeoutResend
     *
     * @return this
     * */
    public Http setTimeoutResend(boolean timeoutResend) {
        this.config.setTimeoutResend(timeoutResend);
        return this;
    }

    /**
     * 发送
     *
     * @param listener listener
     * */
    public void send(Consumer<HttpResponseResult<InputStream>> listener) {
        Type type = this.config.getType();
        if (type == null) {
            throw new BaseException("type is null");
        }
        HttpResponseResult<InputStream> httpResponseResult = new HttpResponseResult<>();
        HttpEntity httpEntity = null;
        try (CloseableHttpClient closeableHttpClient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build(), NoopHostnameVerifier.INSTANCE)).build()) {
            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return type.name();
                }
            };
            httpEntityEnclosingRequestBase.setURI(URI.create(Http.this.config.getUrl()));
            int timeout = this.config.getTimeout();
            httpEntityEnclosingRequestBase.setConfig(RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).setConnectionRequestTimeout(timeout).build());
            for (Header header : this.config.getHeader()) {
                httpEntityEnclosingRequestBase.setHeader(header.getKey(), header.getValue());
            }
            Object body = this.config.getBody();
            ContentType contentType = this.config.getContentType();
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
                        String bodyString = ArrayUtil.toString(stringObjectMap.keySet(), key -> {
                            Object value = map.get(key);
                            if (value == null) {
                                return null;
                            }
                            return key + "=" + ObjectUtil.toString(value);
                        }, "&");
                        httpEntity = new StringEntity(bodyString == null ? "" : bodyString, "UTF-8");
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
            if (this.config.isTimeoutResend() && this.timeoutResendNumber > 0) {
                this.timeoutResendNumber = this.timeoutResendNumber - 1;
                send(listener);
            } else {
                throw new BaseException(e.getMessage());
            }
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
        Function<HttpResponseResult<String>, Boolean> resend = this.config.getResend();
        if (resend != null && ObjectUtil.equals(true, resend.apply(result))) {
            return send();
        }
        return result;
    }

    /**
     * 发送json格式
     *
     * @param config config
     *
     * @return Json
     * */
    public static Json sendByJson(Config config) {
        if (config == null) {
            throw new BaseException("config is null");
        }
        Http http = new Http(config.getUrl(), config.getType());
        http.config = config;
        http.setContentType(ContentType.APPLICATION_JSON);
        return http.send().parse(Json.class).getBody();
    }

}