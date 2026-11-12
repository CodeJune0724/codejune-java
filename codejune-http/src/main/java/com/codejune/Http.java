package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.http.ContentType;
import com.codejune.http.FormData;
import com.codejune.http.Header;
import com.codejune.http.HttpRequest;
import com.codejune.http.HttpResponse;
import com.codejune.http.Type;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Array;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * http组件
 *
 * @author ZJ
 * */
public final class Http {

    private final HttpRequest httpRequest;

    private static final int RESEND_NUMBER = 3;

    private static final int MAX_CLIENT_CACHE = 16;

    private static final Set<String> RESTRICTED_HEADER = Set.of("connection", "content-length", "expect", "host", "upgrade");

    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*\"?([^\";\\s]+)", Pattern.CASE_INSENSITIVE);

    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);

    private static final byte[] EMPTY = new byte[0];

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final ExecutorService CLIENT_CLEANER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "codejune-http-client-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    private static final Map<String, HttpClient> CLIENT_MAP = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, HttpClient> eldest) {
            if (size() <= MAX_CLIENT_CACHE) {
                return false;
            }
            closeClientAsync(eldest.getValue());
            return true;
        }
    });

    private static volatile SSLContext trustAllSSLContext;

    public Http(String url, Type type) {
        this(new HttpRequest(url, type));
    }

    public Http(HttpRequest httpRequest) {
        if (httpRequest == null) {
            throw new BaseException("httpRequest is null");
        }
        this.httpRequest = httpRequest;
        this.httpRequest.addHeaderIfAbsent("Accept", "*/*");
        this.httpRequest.addHeaderIfAbsent("Accept-Encoding", "gzip, deflate");
        this.httpRequest.addHeaderIfAbsent("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        this.httpRequest.addHeaderIfAbsent("Cache-Control", "max-age=0");
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
     * 发送
     *
     * <p>回调返回后响应流仍然打开，由调用方负责关闭，否则会泄漏连接。</p>
     *
     * @param listener listener
     * */
    public void send(Consumer<HttpResponse<InputStream>> listener) {
        if (listener == null) {
            listener = (_) -> {};
        }
        HttpResponse<InputStream> httpResponse = this.execute();
        try {
            listener.accept(httpResponse);
        } finally {
            Closeable.closeNoError(httpResponse.getBody());
        }
    }

    /**
     * 发送
     *
     * @return HttpResponseResult
     * */
    public HttpResponse<String> send() {
        HttpResponse<String> result = new HttpResponse<>();
        this.send(httpResponse -> {
            result.setCode(httpResponse.getCode());
            for (Header header : httpResponse.getHeaderList()) {
                result.addHeader(header.getKey(), header.getValue());
            }
            result.setHttpRequest(httpResponse.getHttpRequest());
            result.setBody(readText(httpResponse));
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
            throw new BaseException("httpRequest is null");
        }
        Http http = new Http(httpRequest);
        if (httpRequest.getContentType() == null) {
            http.getHttpRequest().setContentType(ContentType.APPLICATION_JSON);
        }
        HttpResponse<String> httpResponse = http.send();
        return Json.parse(httpResponse.getBody());
    }

    /**
     * 执行请求
     *
     * @return HttpResponse
     * */
    private HttpResponse<InputStream> execute() {
        int maxAttempt = 1;
        if (this.httpRequest.isTimeoutResend() && this.httpRequest.getType().isIdempotent()) {
            maxAttempt = maxAttempt + RESEND_NUMBER;
        }
        IOException lastError = null;
        for (int attempt = 0; attempt < maxAttempt; attempt++) {
            try {
                return this.executeOnce();
            } catch (IOException e) {
                lastError = e;
                if (!isRetryable(e) || attempt == maxAttempt - 1) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BaseException(e);
            }
        }
        throw new BaseException(lastError);
    }

    /**
     * 执行一次请求
     *
     * @return HttpResponse
     * */
    @SuppressWarnings("resource")
    private HttpResponse<InputStream> executeOnce() throws IOException, InterruptedException {
        HttpRequest request = this.httpRequest;
        Type type = request.getType();
        Object body = request.getBody();
        if (body != null && (type == Type.GET || type == Type.HEAD)) {
            throw new BaseException(type + " 请求不支持请求体，请使用 POST/PUT/PATCH");
        }
        String boundary = "----CodejuneBoundary" + UUID.randomUUID().toString().replace("-", "");
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(toUri(request.getUrl()))
                .method(type.name(), bodyPublisher(request, boundary));
        if (request.getReadTimeout() > 0) {
            builder.timeout(Duration.ofMillis(request.getReadTimeout()));
        }
        String contentType = contentType(request, boundary);
        for (Header header : request.getHeader()) {
            if (StringUtil.isEmpty(header.getKey()) || header.getValue() == null) {
                continue;
            }
            if (RESTRICTED_HEADER.contains(header.getKey().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (contentType != null && "content-type".equalsIgnoreCase(header.getKey())) {
                continue;
            }
            try {
                builder.setHeader(header.getKey(), header.getValue());
            } catch (IllegalArgumentException e) {
                throw new BaseException(e);
            }
        }
        if (contentType != null) {
            builder.setHeader("Content-Type", contentType);
        }
        java.net.http.HttpResponse<InputStream> raw;
        try {
            raw = client(request).send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofInputStream());
        } catch (IllegalArgumentException e) {
            throw new BaseException(e);
        }
        HttpResponse<InputStream> result = new HttpResponse<>();
        result.setCode(raw.statusCode());
        raw.headers().map().forEach((key, values) -> {
            for (String value : values) {
                result.addHeader(key, value);
            }
        });
        result.setHttpRequest(request);
        InputStream inputStream = raw.body();
        try {
            inputStream = decodeContent(inputStream, result.getHeader("Content-Encoding"));
        } catch (IOException e) {
            Closeable.closeNoError(inputStream);
            throw new BaseException(e);
        }
        result.setBody(inputStream);
        return result;
    }

    /**
     * 是否需要重试
     *
     * @param e 异常
     *
     * @return 需要重试返回true
     * */
    private static boolean isRetryable(IOException e) {
        if (e instanceof javax.net.ssl.SSLException) {
            return false;
        }
        return !(e instanceof UnknownHostException);
    }

    /**
     * 获取HttpClient
     *
     * @param request request
     *
     * @return HttpClient
     * */
    private static HttpClient client(HttpRequest request) {
        Proxy proxy = request.getProxy();
        String key = (proxy == null ? "" : String.valueOf(proxy.address()))
                + "|" + request.getConnectTimeout()
                + "|" + request.isRedirect()
                + "|" + request.isTrustAllCertificate();
        return CLIENT_MAP.computeIfAbsent(key, _ -> buildClient(request));
    }

    /**
     * 异步关闭HttpClient
     *
     * @param httpClient httpClient
     * */
    private static void closeClientAsync(HttpClient httpClient) {
        CLIENT_CLEANER.execute(() -> Closeable.closeNoError(httpClient));
    }

    /**
     * 创建HttpClient
     *
     * @param request request
     *
     * @return HttpClient
     * */
    private static HttpClient buildClient(HttpRequest request) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(request.isRedirect() ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);
        if (request.getConnectTimeout() > 0) {
            builder.connectTimeout(Duration.ofMillis(request.getConnectTimeout()));
        }
        Proxy proxy = request.getProxy();
        if (proxy != null) {
            builder.proxy(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return List.of(proxy);
                }
                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
            });
        }
        if (request.isTrustAllCertificate()) {
            builder.sslContext(trustAllContext());
        }
        return builder.build();
    }

    /**
     * 信任所有证书的SSLContext
     *
     * @return SSLContext
     * */
    private static SSLContext trustAllContext() {
        SSLContext result = trustAllSSLContext;
        if (result == null) {
            synchronized (Http.class) {
                result = trustAllSSLContext;
                if (result == null) {
                    try {
                        result = SSLContext.getInstance("TLS");
                        result.init(null, new TrustManager[]{new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }}, SECURE_RANDOM);
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }
                    trustAllSSLContext = result;
                }
            }
        }
        return result;
    }

    /**
     * 获取请求体
     *
     * @param request request
     * @param boundary multipart边界
     *
     * @return BodyPublisher
     * */
    private static java.net.http.HttpRequest.BodyPublisher bodyPublisher(HttpRequest request, String boundary) {
        Object body = request.getBody();
        switch (body) {
            case null -> {
                return java.net.http.HttpRequest.BodyPublishers.noBody();
            }
            case byte[] bytes -> {
                return java.net.http.HttpRequest.BodyPublishers.ofByteArray(bytes);
            }
            case File file -> {
                try {
                    return java.net.http.HttpRequest.BodyPublishers.ofFile(file.toPath());
                } catch (FileNotFoundException e) {
                    throw new BaseException(e);
                }
            }
            case InputStream inputStream -> {
                return java.net.http.HttpRequest.BodyPublishers.ofInputStream(() -> inputStream);
            }
            default -> {
            }
        }
        ContentType contentType = effectiveContentType(request);
        if (contentType == ContentType.FORM_DATA) {
            return multipartPublisher(toFormData(body), boundary);
        }
        if (contentType == ContentType.FORM_URLENCODED) {
            return java.net.http.HttpRequest.BodyPublishers.ofString(formUrlEncoded(body));
        }
        if (contentType == ContentType.APPLICATION_JSON) {
            return java.net.http.HttpRequest.BodyPublishers.ofString(Json.toString(body));
        }
        return java.net.http.HttpRequest.BodyPublishers.ofString(ObjectUtil.toString(body));
    }

    /**
     * 获取实际生效的ContentType
     *
     * @param request request
     *
     * @return ContentType
     * */
    private static ContentType effectiveContentType(HttpRequest request) {
        ContentType contentType = request.getContentType();
        if (contentType != null) {
            return contentType;
        }
        Object body = request.getBody();
        if (body instanceof FormData) {
            return ContentType.FORM_DATA;
        }
        if (body instanceof Map<?, ?>) {
            return ContentType.FORM_URLENCODED;
        }
        if (body instanceof byte[] || body instanceof File || body instanceof InputStream) {
            return ContentType.BINARY;
        }
        return null;
    }

    /**
     * 获取Content-Type请求头
     *
     * @param request request
     * @param boundary multipart边界
     *
     * @return Content-Type
     * */
    private static String contentType(HttpRequest request, String boundary) {
        ContentType contentType = effectiveContentType(request);
        if (contentType == null) {
            return null;
        }
        return switch (contentType) {
            case FORM_DATA -> "multipart/form-data; boundary=" + boundary;
            case TEXT_PLAIN -> "text/plain; charset=UTF-8";
            default -> contentType.getContentType();
        };
    }

    /**
     * 转换FormData
     *
     * @param body body
     *
     * @return FormData
     * */
    private static FormData toFormData(Object body) {
        if (body instanceof FormData formData) {
            return formData;
        }
        try {
            if (body instanceof Map<?, ?> map) {
                FormData result = ObjectUtil.parse(MapUtil.parse(map, String.class, Object.class), FormData.class);
                return result == null ? new FormData() : result;
            }
            FormData result = ObjectUtil.parse(MapUtil.parse(body, String.class, Object.class), FormData.class);
            return result == null ? new FormData() : result;
        } catch (RuntimeException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取multipart的BodyPublisher
     *
     * @param formData formData
     * @param boundary boundary
     *
     * @return BodyPublisher
     * */
    private static java.net.http.HttpRequest.BodyPublisher multipartPublisher(FormData formData, String boundary) {
        List<FormData.FormDataItem> itemList = formData.getFormDataItem();
        long length = multipartLength(itemList, boundary);
        java.net.http.HttpRequest.BodyPublisher publisher = java.net.http.HttpRequest.BodyPublishers.ofInputStream(() -> multipartStream(itemList, boundary));
        if (length < 0) {
            return publisher;
        }
        return java.net.http.HttpRequest.BodyPublishers.fromPublisher(publisher, length);
    }

    /**
     * 获取multipart的总长度
     *
     * @param itemList itemList
     * @param boundary boundary
     *
     * @return 长度，未知返回-1
     * */
    private static long multipartLength(List<FormData.FormDataItem> itemList, String boundary) {
        long length = closingBoundary(boundary).length;
        for (FormData.FormDataItem item : itemList) {
            long dataLength = dataLength(item);
            if (dataLength < 0) {
                return -1;
            }
            byte[] header = multipartHeader(item, boundary);
            length = length + header.length + dataLength + CRLF.length;
        }
        return length;
    }

    /**
     * 获取multipart的流
     *
     * @param itemList itemList
     * @param boundary boundary
     *
     * @return InputStream
     * */
    private static InputStream multipartStream(List<FormData.FormDataItem> itemList, String boundary) {
        List<Part> parts = new ArrayList<>();
        for (FormData.FormDataItem item : itemList) {
            parts.add(new Part(new ByteArrayInputStream(multipartHeader(item, boundary)), false));
            parts.add(new Part(dataStream(item), item.getData() instanceof File));
            parts.add(new Part(new ByteArrayInputStream(CRLF), false));
        }
        parts.add(new Part(new ByteArrayInputStream(closingBoundary(boundary)), false));
        return new CompositeInputStream(parts);
    }

    /**
     * 获取multipart的结尾
     *
     * @param boundary boundary
     *
     * @return byte[]
     * */
    private static byte[] closingBoundary(String boundary) {
        return ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取multipart中单个分片的头
     *
     * @param item item
     * @param boundary boundary
     *
     * @return byte[]
     * */
    private static byte[] multipartHeader(FormData.FormDataItem item, String boundary) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"").append(escapeQuoted(item.getName())).append('"');
        String fileName = item.getFileName();
        if (!StringUtil.isEmpty(fileName)) {
            if (isAscii(fileName)) {
                builder.append("; filename=\"").append(escapeQuoted(fileName)).append('"');
            } else {
                builder.append("; filename*=UTF-8''").append(encodeRfc5987(fileName));
            }
        }
        builder.append("\r\n");
        ContentType contentType = item.getContentType();
        if (contentType == ContentType.TEXT_PLAIN) {
            builder.append("Content-Type: text/plain; charset=UTF-8\r\n");
        } else if (contentType != null && contentType != ContentType.FORM_DATA) {
            builder.append("Content-Type: ").append(contentType.getContentType()).append("\r\n");
        }
        builder.append("\r\n");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取分片数据长度
     *
     * @param item item
     *
     * @return 长度，未知返回-1
     * */
    private static long dataLength(FormData.FormDataItem item) {
        Object data = item.getData();
        return switch (data) {
            case null -> 0;
            case byte[] bytes -> bytes.length;
            case File file -> file.length();
            case InputStream _ -> -1;
            default -> ObjectUtil.toString(data).getBytes(StandardCharsets.UTF_8).length;
        };
    }

    /**
     * 获取分片数据流
     *
     * @param item item
     *
     * @return InputStream
     * */
    private static InputStream dataStream(FormData.FormDataItem item) {
        Object data = item.getData();
        switch (data) {
            case null -> {
                return new ByteArrayInputStream(EMPTY);
            }
            case byte[] bytes -> {
                return new ByteArrayInputStream(bytes);
            }
            case File file -> {
                try {
                    return Files.newInputStream(file.toPath());
                } catch (IOException e) {
                    throw new BaseException(e);
                }
            }
            case InputStream inputStream -> {
                return inputStream;
            }
            default -> {
            }
        }
        return new ByteArrayInputStream(ObjectUtil.toString(data).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 转成url编码的表单
     *
     * @param body body
     *
     * @return 表单字符串
     * */
    private static String formUrlEncoded(Object body) {
        if (body instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String key = ObjectUtil.toString(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof Collection<?> collection) {
                    for (Object item : collection) {
                        appendFormValue(builder, key, item);
                    }
                } else if (value != null && value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        appendFormValue(builder, key, Array.get(value, i));
                    }
                } else {
                    appendFormValue(builder, key, value);
                }
            }
            return builder.toString();
        }
        return ObjectUtil.toString(body);
    }

    /**
     * 追加一个表单参数
     *
     * @param builder builder
     * @param key key
     * @param value value
     * */
    private static void appendFormValue(StringBuilder builder, String key, Object value) {
        if (value == null) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append('&');
        }
        builder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        builder.append('=');
        builder.append(URLEncoder.encode(ObjectUtil.toString(value), StandardCharsets.UTF_8));
    }

    /**
     * 解压响应内容
     *
     * @param inputStream inputStream
     * @param contentEncoding contentEncoding
     *
     * @return InputStream
     * */
    private static InputStream decodeContent(InputStream inputStream, Header contentEncoding) throws IOException {
        if (inputStream == null || contentEncoding == null || StringUtil.isEmpty(contentEncoding.getValue())) {
            return inputStream;
        }
        String[] encodingArray = contentEncoding.getValue().split(",");
        for (int i = encodingArray.length - 1; i >= 0; i--) {
            String encoding = encodingArray[i].trim().toLowerCase(Locale.ROOT);
            switch (encoding) {
                case "", "identity" -> {}
                case "gzip", "x-gzip" -> inputStream = gzipStream(inputStream);
                case "deflate" -> inputStream = deflateStream(inputStream);
                default -> throw new BaseException("不支持的Content-Encoding: " + encoding);
            }
        }
        return inputStream;
    }

    /**
     * gzip解压（没有gzip头时原样返回，兼容HEAD、204等没有响应体的情况）
     *
     * @param inputStream inputStream
     *
     * @return InputStream
     * */
    private static InputStream gzipStream(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
        byte[] head = new byte[2];
        int size = pushbackInputStream.read(head);
        if (size > 0) {
            pushbackInputStream.unread(head, 0, size);
        }
        if (size < 2 || (head[0] & 0xFF) != 0x1F || (head[1] & 0xFF) != 0x8B) {
            return pushbackInputStream;
        }
        return new GZIPInputStream(pushbackInputStream);
    }

    /**
     * deflate解压（兼容zlib和raw两种格式）
     *
     * @param inputStream inputStream
     *
     * @return InputStream
     * */
    private static InputStream deflateStream(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
        byte[] head = new byte[2];
        int size = pushbackInputStream.read(head);
        if (size > 0) {
            pushbackInputStream.unread(head, 0, size);
        }
        boolean zlib = size == 2 && (head[0] & 0x0F) == 8 && (((head[0] & 0xFF) << 8) | (head[1] & 0xFF)) % 31 == 0;
        return new InflaterInputStream(pushbackInputStream, new Inflater(!zlib));
    }

    /**
     * 读取响应文本
     *
     * @param httpResponse httpResponse
     *
     * @return 文本
     * */
    private static String readText(HttpResponse<InputStream> httpResponse) {
        InputStream inputStream = httpResponse.getBody();
        if (inputStream == null) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new BaseException(e);
        }
        if (bytes.length == 0) {
            return "";
        }
        return decodeText(bytes, charset(httpResponse));
    }

    /**
     * 获取响应字符集
     *
     * @param httpResponse httpResponse
     *
     * @return Charset
     * */
    private static Charset charset(HttpResponse<?> httpResponse) {
        Header contentType = httpResponse.getHeader("Content-Type");
        if (contentType == null || StringUtil.isEmpty(contentType.getValue())) {
            return StandardCharsets.UTF_8;
        }
        Matcher matcher = CHARSET_PATTERN.matcher(contentType.getValue());
        if (!matcher.find()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(matcher.group(1).trim());
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 解码文本（识别BOM）
     *
     * @param bytes bytes
     * @param charset 默认字符集
     *
     * @return 文本
     * */
    private static String decodeText(byte[] bytes, Charset charset) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        return new String(bytes, charset);
    }

    /**
     * 转成URI
     *
     * @param url url
     *
     * @return URI
     * */
    private static URI toUri(String url) {
        if (StringUtil.isEmpty(url)) {
            throw new BaseException("url is empty");
        }
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            try {
                return URI.create(url.replace(" ", "%20"));
            } catch (IllegalArgumentException e1) {
                throw new BaseException(e1);
            }
        }
    }

    /**
     * 转义Content-Disposition中的引号内容
     *
     * @param value value
     *
     * @return 转义后的内容
     * */
    private static String escapeQuoted(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
    }

    /**
     * 是否全是ASCII字符
     *
     * @param value value
     *
     * @return 全是ASCII返回true
     * */
    private static boolean isAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    /**
     * RFC5987编码
     *
     * @param value value
     *
     * @return 编码后的内容
     * */
    private static String encodeRfc5987(String value) {
        StringBuilder builder = new StringBuilder();
        for (byte item : value.getBytes(StandardCharsets.UTF_8)) {
            int code = item & 0xFF;
            if ((code >= 'a' && code <= 'z') || (code >= 'A' && code <= 'Z') || (code >= '0' && code <= '9')
                    || code == '!' || code == '#' || code == '$' || code == '&' || code == '+' || code == '-'
                    || code == '.' || code == '^' || code == '_' || code == '`' || code == '|' || code == '~') {
                builder.append((char) code);
            } else {
                builder.append('%').append(String.format("%02X", code));
            }
        }
        return builder.toString();
    }

    /**
     * 流的一部分
     *
     * @param stream 流
     * @param close 是否由本类关闭
     * */
    private record Part(InputStream stream, boolean close) {}

    /**
     * 把多个流串联成一个流
     *
     * @author ZJ
     * */
    private static final class CompositeInputStream extends InputStream {

        private final Iterator<Part> iterator;

        private Part current;

        private boolean closed;

        private CompositeInputStream(List<Part> parts) {
            this.iterator = parts.iterator();
        }

        @Override
        public int read() throws IOException {
            while (true) {
                if (this.closed) {
                    throw new IOException("stream closed");
                }
                if (this.current == null) {
                    if (!this.iterator.hasNext()) {
                        return -1;
                    }
                    this.current = this.iterator.next();
                }
                int result = this.current.stream().read();
                if (result != -1) {
                    return result;
                }
                this.finishCurrent();
            }
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            while (true) {
                if (this.closed) {
                    throw new IOException("stream closed");
                }
                if (this.current == null) {
                    if (!this.iterator.hasNext()) {
                        return -1;
                    }
                    this.current = this.iterator.next();
                }
                int result = this.current.stream().read(bytes, offset, length);
                if (result != -1) {
                    return result;
                }
                this.finishCurrent();
            }
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.finishCurrent();
            while (this.iterator.hasNext()) {
                Part part = this.iterator.next();
                if (part.close()) {
                    Closeable.closeNoError(part.stream());
                }
            }
        }

        private void finishCurrent() {
            Part part = this.current;
            this.current = null;
            if (part != null && part.close()) {
                Closeable.closeNoError(part.stream());
            }
        }

    }

}