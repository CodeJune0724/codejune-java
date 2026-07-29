package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.io.reader.InputStreamReader;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.*;
import com.codejune.http.*;
import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * http组件
 *
 * @author ZJ
 */
public final class Http {

    private HttpRequest httpRequest;

    private int timeoutResendNumber = 10;

    public Http(String url, Type type) {
        this.httpRequest = new HttpRequest(url, type);
        this.addHeader("Accept", "*/*");
        this.addHeader("Accept-Encoding", "gzip, deflate, br, zstd");
        this.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        this.addHeader("Cache-Control", "max-age=0");
        this.addHeader("Connection", "Keep-Alive");
        this.addHeader("Host", RegexUtil.find("//(.+?)/", url, 1));
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
     * 设置ContentType
     *
     * @param contentType contentType
     *
     * @return this
     * */
    public Http setContentType(ContentType contentType) {
        this.httpRequest.setContentType(contentType);
        return this;
    }

    /**
     * 添加User-Agent
     *
     * @return this
     * */
    public Http addUserAgent() {
        this.httpRequest.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");
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
        this.httpRequest.setBody(body);
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
        this.httpRequest.addHeader(key, value);
        return this;
    }

    /**
     * 移除请求头
     *
     * @param key key
     *
     * @return this
     * */
    public Http deleteHeader(String key) {
        this.httpRequest.deleteHeader(key);
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
        this.httpRequest.setTimeout(timeout);
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
        this.httpRequest.setTimeoutResend(timeoutResend);
        return this;
    }

    /**
     * 设置代理
     *
     * @param host host
     * @param port port
     *
     * @return this
     * */
    public Http setProxy(String host, int port) {
        this.httpRequest.setProxy(host, port);
        return this;
    }

    /**
     * 设置重新向
     *
     * @param redirect redirect
     * */
    public Http setRedirect(boolean redirect) {
        this.httpRequest.setRedirect(redirect);
        return this;
    }

    /**
     * 发送
     *
     * @param listener listener
     * */
    public void send(Consumer<HttpResponse<InputStream>> listener) {
        if (listener == null) {
            listener = (_) -> {};
        }
        HttpURLConnection httpURLConnection = null;
        try {
            Proxy proxy = this.httpRequest.getProxy();
            if (proxy == null) {
                httpURLConnection = (HttpURLConnection) new URI(this.httpRequest.getUrl()).toURL().openConnection();
            } else {
                httpURLConnection = (HttpURLConnection) new URI(this.httpRequest.getUrl()).toURL().openConnection(proxy);
            }
            if (httpURLConnection instanceof HttpsURLConnection httpsURLConnection) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[] {
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            }
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                }, new java.security.SecureRandom());
                httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsURLConnection.setHostnameVerifier((_, _) -> true);
            }
            httpURLConnection.setRequestMethod(this.httpRequest.getType().name());
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(false);
            if (this.httpRequest.getTimeout() > 0) {
                httpURLConnection.setConnectTimeout(this.httpRequest.getTimeout());
                httpURLConnection.setReadTimeout(this.httpRequest.getTimeout());
            }
            for (Header header : this.httpRequest.getHeader()) {
                httpURLConnection.addRequestProperty(header.getKey(), header.getValue());
            }
            ContentType contentType = this.httpRequest.getContentType();
            String boundary = UUID.randomUUID().toString().replace("-", "");
            if (contentType != null) {
                if (contentType == ContentType.FORM_DATA) {
                    httpURLConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                } else {
                    httpURLConnection.addRequestProperty("Content-Type", contentType.getContentType());
                }
            }
            httpURLConnection.connect();
            Object body = this.httpRequest.getBody();
            if (body != null) {
                if (contentType == ContentType.APPLICATION_JSON) {
                    try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                        outputStream.write(Json.toString(body).getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                } else if (contentType == ContentType.FORM_DATA) {
                    FormData formData;
                    if (body instanceof FormData bodyFormData) {
                        formData = bodyFormData;
                    } else {
                        formData = ObjectUtil.parse(MapUtil.parse(body, String.class, Object.class), FormData.class);
                    }
                    if (formData != null) {
                        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                            for (FormData.FormDataItem formDataItem : formData.getFormDataItem()) {
                                outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                                outputStream.write(("Content-Disposition: form-data; name=\"" + formDataItem.getName() + "\"" + (StringUtil.isEmpty(formDataItem.getFileName()) ? "" : "; filename=\"" + formDataItem.getFileName()) + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                                if (formDataItem.getContentType() == ContentType.DEFAULT_BINARY) {
                                    outputStream.write("Content-Type: application/octet-stream; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
                                }
                                outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
                                if (formDataItem.getContentType() == ContentType.DEFAULT_BINARY) {
                                    try (InputStreamReader inputStreamReader = new InputStreamReader(ObjectUtil.parse(formDataItem.getData(), InputStream.class))) {
                                        inputStreamReader.read(byteBuffer -> new OutputStreamWriter(outputStream).write(byteBuffer));
                                    }
                                }
                                if (formDataItem.getContentType() == ContentType.TEXT_PLAIN) {
                                    outputStream.write(ObjectUtil.parse(formDataItem.getData(), String.class).getBytes(StandardCharsets.UTF_8));
                                }
                                outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
                            }
                            outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        }
                    }
                } else if (contentType == ContentType.FORM_URLENCODED) {
                    String urlEncode;
                    if (body instanceof Map<?,?> map) {
                        Map<String, Object> stringObjectMap = MapUtil.parse(map, String.class, Object.class);
                        if (stringObjectMap == null) {
                            stringObjectMap = new HashMap<>();
                        }
                        urlEncode = ArrayUtil.toString(stringObjectMap.keySet(), key -> {
                            Object value = map.get(key);
                            if (value == null) {
                                return null;
                            }
                            return key + "=" + ObjectUtil.toString(value);
                        }, "&");
                    } else {
                        urlEncode = ObjectUtil.toString(body);
                    }
                    if (urlEncode != null) {
                        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                            outputStream.write(urlEncode.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                } else {
                    try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                        outputStream.write(ObjectUtil.toString(body).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            HttpResponse<InputStream> result = new HttpResponse<>();
            result.setCode(httpURLConnection.getResponseCode());
            Map<String, List<String>> responseHeader = httpURLConnection.getHeaderFields();
            for (String key : responseHeader.keySet()) {
                List<String> header = responseHeader.get(key);
                for (String item : header) {
                    result.addHeader(key, item);
                }
            }
            InputStream inputStream;
            if (result.isFlag()) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }
            result.setBody(inputStream);
            result.setHttpRequest(this.httpRequest);
            try {
                if (this.httpRequest.isRedirect() && result.getCode() == 302) {
                    AtomicReference<HttpResponse<InputStream>> redirectHttpResponse = new AtomicReference<>(result);
                    Consumer<HttpResponse<InputStream>> finalListener = listener;
                    while (redirectHttpResponse.get().getCode() == 302) {
                        Header locationHeader = redirectHttpResponse.get().getHeader("Location");
                        if (locationHeader == null) {
                            throw new BaseException("Location is null");
                        }
                        String location = locationHeader.getValue();
                        if (location.startsWith("/")) {
                            location = RegexUtil.find("^(https?://)?[^/?#]+", redirectHttpResponse.get().getHttpRequest().getUrl(), 0) + location;
                        }
                        new Http(location, Type.GET)
                                .addUserAgent()
                                .send((httpResponse) -> {
                                    redirectHttpResponse.set(httpResponse);
                                    if (httpResponse.getCode() != 302) {
                                        finalListener.accept(httpResponse);
                                    }
                                });
                    }
                } else {
                    listener.accept(result);
                }
            } finally {
                Closeable.closeNoError(inputStream);
            }
        } catch (Exception e) {
            if (this.httpRequest.isTimeoutResend() && this.timeoutResendNumber > 0) {
                this.timeoutResendNumber = this.timeoutResendNumber - 1;
                send(listener);
            } else {
                throw new BaseException(e);
            }
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * 发送
     *
     * @return HttpResponseResult
     * */
    public HttpResponse<String> send() {
        HttpResponse<String> result = new HttpResponse<>();
        send(httpResponseResult -> {
            result.build(httpResponseResult);
            if (httpResponseResult.getBody() != null) {
                String body;
                Header header = httpResponseResult.getHeader("Content-Encoding");
                if (header != null && "gzip".equals(header.getValue())) {
                    try {
                        GZIPInputStream gzipInputStream = new GZIPInputStream(httpResponseResult.getBody());
                        BufferedReader bufferedReader = new BufferedReader(new java.io.InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        body = stringBuilder.toString();
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }
                } else {
                    body = new TextInputStreamReader(httpResponseResult.getBody()).getData();
                }
                result.setBody(body);
            }
        });
        return result;
    }

    /**
     * 发送json格式
     *
     * @param httpRequest httpRequest
     *
     * @return Json
     * */
    public static Json sendByJson(HttpRequest httpRequest) {
        if (httpRequest == null) {
            throw new BaseException("config is null");
        }
        Http http = new Http(httpRequest.getUrl(), httpRequest.getType());
        http.httpRequest = httpRequest;
        http.setContentType(ContentType.APPLICATION_JSON);
        return http.send().parse(Json.class).getBody();
    }

}