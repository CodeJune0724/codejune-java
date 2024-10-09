package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.*;
import com.codejune.http.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
        this.addHeader("accept-encoding", "gzip, deflate, br, zstd");
        this.addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        this.addHeader("cache-control", "max-age=0");
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
    public void send(final Consumer<HttpResponseResult<InputStream>> listener) {
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().build();
            sslContext.init(null, new TrustManager[] {new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
                @Override
                public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {}
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {}
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {}
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {}
            }}, new SecureRandom());
        } catch (Exception e) {
            throw new BaseException(e);
        }
        int timeout = this.config.getTimeout();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout > 0 ? Timeout.ofMilliseconds(timeout) : null).setResponseTimeout(timeout > 0 ? Timeout.ofMilliseconds(timeout) : null).setRedirectsEnabled(false).build();
        HttpEntity httpEntity = null;
        HttpResponseResult<InputStream> httpResponseResult = new HttpResponseResult<>();
        try (
                PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext)).build();
                CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).setDefaultRequestConfig(requestConfig).build()
        ) {
            BasicClassicHttpRequest basicClassicHttpRequest = new BasicClassicHttpRequest(this.config.getType().name(), URI.create(Http.this.config.getUrl()));
            for (Header header : this.config.getHeader()) {
                basicClassicHttpRequest.setHeader(header.getKey(), header.getValue());
            }
            Object body = this.config.getBody();
            if (body != null) {
                ContentType contentType = this.config.getContentType();
                if (contentType == ContentType.APPLICATION_JSON) {
                    httpEntity = new StringEntity(Json.toString(body), StandardCharsets.UTF_8);
                } else if (contentType == ContentType.FORM_DATA) {
                    FormData formData;
                    if (body instanceof FormData bodyFormData) {
                        formData = bodyFormData;
                    } else {
                        formData = ObjectUtil.parse(MapUtil.parse(body, String.class, Object.class), FormData.class);
                    }
                    if (formData != null) {
                        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                        for (FormData.FormDataItem formDataItem : formData.getFormDataItem()) {
                            if (formDataItem.getContentType() == ContentType.DEFAULT_BINARY) {
                                multipartEntityBuilder.addBinaryBody(formDataItem.getName(), ObjectUtil.parse(formDataItem.getData(), InputStream.class), org.apache.hc.core5.http.ContentType.DEFAULT_BINARY, formDataItem.getFileName());
                            } else {
                                multipartEntityBuilder.addTextBody(formDataItem.getName(), ObjectUtil.toString(formDataItem.getData()));
                            }
                        }
                        httpEntity = multipartEntityBuilder.build();
                    }
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
                        httpEntity = new StringEntity(bodyString == null ? "" : bodyString, StandardCharsets.UTF_8);
                    } else {
                        httpEntity = new StringEntity(ObjectUtil.toString(body), StandardCharsets.UTF_8);
                    }
                } else {
                    httpEntity = new StringEntity(ObjectUtil.toString(body), StandardCharsets.UTF_8);
                }
            }
            basicClassicHttpRequest.setEntity(httpEntity);
            closeableHttpClient.execute(basicClassicHttpRequest, response -> {
                if (listener == null) {
                    return null;
                }
                httpResponseResult.setCode(response.getCode());
                for (org.apache.hc.core5.http.Header header : response.getHeaders()) {
                    httpResponseResult.addHeader(header.getName(), header.getValue());
                }
                httpResponseResult.setBody(response.getEntity().getContent());
                listener.accept(httpResponseResult);
                return null;
            });
        } catch (Exception e) {
            if (this.config.isTimeoutResend() && this.timeoutResendNumber > 0) {
                this.timeoutResendNumber = this.timeoutResendNumber - 1;
                send(listener);
            } else {
                throw new BaseException(e.getMessage());
            }
        } finally {
            Closeable.closeNoError(httpResponseResult.getBody());
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