package com.codejune;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.*;
import com.codejune.http.ContentType;
import com.codejune.http.HttpResponseResult;
import com.codejune.http.HttpResponseResultHandler;
import com.codejune.http.Type;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * http组件
 *
 * @author ZJ
 */
public final class Http {

    private final String url;

    private final Type type;

    private final Map<String, String> header = new HashMap<>();

    private ContentType contentType;

    private Object body;

    {
        header.put("accept", "*/*");
        header.put("connection", "Keep-Alive");
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

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
        String key = "Content-type";
        if (this.contentType == null) {
            this.header.remove(key);
        } else {
            if (contentType != ContentType.FORM_DATA) {
                this.header.put(key, contentType.getContentType());
            }
        }
    }

    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * 添加请求头
     *
     * @param key 键
     * @param value 值
     * */
    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    /**
     * 发送
     *
     * @param httpResponseResultHandler httpResponseResultHandler
     * */
    public void send(HttpResponseResultHandler httpResponseResultHandler) {
        CloseableHttpClient closeableHttpClient = null;
        CloseableHttpResponse closeableHttpResponse = null;
        HttpEntity httpEntity = null;
        HttpResponseResult<InputStream> httpResponseResult = new HttpResponseResult<>();
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return type.name();
                }
            };
            httpEntityEnclosingRequestBase.setURI(URI.create(url));
            httpEntityEnclosingRequestBase.setConfig(RequestConfig.custom().build());

            // 请求头
            Set<String> keySet = header.keySet();
            for (String key : keySet) {
                httpEntityEnclosingRequestBase.setHeader(key, header.get(key));
            }

            // 请求体
            if (body != null) {
                switch (contentType) {
                    case APPLICATION_JSON:
                        httpEntity = new StringEntity(JsonUtil.toJsonString(body), "UTF-8");
                        break;
                    case FORM_DATA:
                        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
                        Map<String, Object> mapBody = MapUtil.transformGeneric(MapUtil.parse(body), String.class, Object.class);
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
                        httpEntity = multipartEntityBuilder.build();
                        break;
                    default:
                        httpEntity = new StringEntity(ObjectUtil.toString(body), "UTF-8");
                        break;
                }
            }
            httpEntityEnclosingRequestBase.setEntity(httpEntity);

            // 执行
            closeableHttpResponse = closeableHttpClient.execute(httpEntityEnclosingRequestBase);

            httpResponseResult.setCode(closeableHttpResponse.getStatusLine().getStatusCode());
            Header[] allHeaders = closeableHttpResponse.getAllHeaders();
            for (Header header : allHeaders) {
                httpResponseResult.addHeader(header.getName(), header.getValue());
            }
            httpResponseResult.setBody(closeableHttpResponse.getEntity().getContent());

            if (httpResponseResultHandler == null) {
                httpResponseResultHandler = httpResponseResult1 -> {};
            }
            httpResponseResultHandler.handler(httpResponseResult);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(httpResponseResult.getBody());
            try {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        return result;
    }

}