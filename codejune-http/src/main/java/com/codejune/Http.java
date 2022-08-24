package com.codejune;

import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.DownloadHandler;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.JsonUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.http.HttpResponseResult;
import com.codejune.http.HttpResponseResultHandler;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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

    private Map<String, String> header = new LinkedHashMap<>();

    public Http(String url, Type type) {
        this.url = url;
        this.type = type;

        // 默认请求头
        header.put("accept", "*/*");
        header.put("connection", "Keep-Alive");
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
     * 设置请求头
     *
     * @param header header
     * */
    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    /**
     * 设置json请求头
     * */
    public void setContentTypeByJson() {
        this.addHeader("Content-Type", "application/json");
    }

    /**
     * 发送
     *
     * @param data 数据
     * @param httpResponseResultHandler 发送结果处理
     * */
    public void send(String data, HttpResponseResultHandler httpResponseResultHandler) {
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            HttpResponseResult<InputStream> httpResponseResult = new HttpResponseResult<>();

            URL url = new URL(this.url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(type.name());

            // 设置请求头
            Set<String> keys = this.header.keySet();
            for (String key : keys) {
                httpURLConnection.setRequestProperty(key, this.header.get(key));
            }

            // 开启输入输出
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // 输出数据
            if (!StringUtil.isEmpty(data)) {
                outputStream = httpURLConnection.getOutputStream();
                outputStream.write(data.getBytes());
            }

            int status = httpURLConnection.getResponseCode();
            httpResponseResult.setCode(status);
            if (status == 200) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }

            // 获取响应头
            Set<String> responseHeaderKeySet = httpURLConnection.getHeaderFields().keySet();
            for (String key : responseHeaderKeySet) {
                if (StringUtil.isEmpty(key)) {
                    continue;
                }
                httpResponseResult.addHeader(key, httpURLConnection.getHeaderField(key));
            }

            // 获取响应数据
            httpResponseResult.setBody(inputStream);

            // 处理
            if (httpResponseResultHandler == null) {
                httpResponseResultHandler = httpResponseResult1 -> {};
            }
            httpResponseResultHandler.handler(httpResponseResult);
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            IOUtil.close(inputStream);
            IOUtil.close(outputStream);
        }
    }

    /**
     * 发送body
     *
     * @param data 请求数据
     *
     * @return 响应结果
     * */
    public HttpResponseResult<String> sendBody(String data) {
        if (JsonUtil.isJson(data)) {
            setContentTypeByJson();
        }
        HttpResponseResult<String> result = new HttpResponseResult<>();
        send(data, httpResponseResult -> {
            result.assignment(httpResponseResult);
            result.setBody(IOUtil.toString(httpResponseResult.getBody()));
        });
        return result;
    }

    /**
     * 发送表单数据
     *
     * @param formData 表单数据
     *
     * @return 响应
     * */
    public HttpResponseResult<String> sendFormData(Map<String, Object> formData) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpEntity httpEntity = null;
        HttpResponseResult<String> result = new HttpResponseResult<>();
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(this.url);
            httpPost.setConfig(RequestConfig.custom().setConnectTimeout(5000).build());
            Set<String> keySet = this.header.keySet();
            for (String key : keySet) {
                httpPost.setHeader(key, header.get(key));
            }
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (String key : formData.keySet()) {
                Object value = formData.get(key);
                if (value == null) {
                    continue;
                }
                if (value instanceof File) {
                    FileBody fileBody = new FileBody((File) value);
                    multipartEntityBuilder = multipartEntityBuilder.addPart(key, fileBody);
                } else {
                    multipartEntityBuilder.addPart(key, new StringBody(value.toString(), ContentType.MULTIPART_FORM_DATA));
                }
            }
            HttpEntity reqEntity = multipartEntityBuilder.build();
            httpPost.setEntity(reqEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            result.setCode(statusCode);
            Header[] allHeaders = response.getAllHeaders();
            for (Header header : allHeaders) {
                result.addHeader(header.getName(), header.getValue());
            }
            httpEntity = response.getEntity();
            String responseBody = null;
            if (httpEntity != null) {
                responseBody = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            }
            result.setBody(responseBody);
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (httpEntity != null) {
                    EntityUtils.consume(httpEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载文件
     *
     * @param downloadHandler downloadHandler
     * */
    public void download(DownloadHandler downloadHandler) {
        if (downloadHandler == null) {
            downloadHandler = inputStream -> {};
        }
        DownloadHandler finalDownloadHandler = downloadHandler;
        send(null, httpResponseResult -> {
            if (httpResponseResult.isFlag()) {
                finalDownloadHandler.download(httpResponseResult.getBody());
            } else {
                throw new InfoException(IOUtil.toString(httpResponseResult.getBody()));
            }
        });
    }

    public enum Type {
        GET,
        POST,
        PUT,
        DELETE
    }

}