package com.codejune.tool.cloudstoragedownload;

import com.codejune.Http;
import com.codejune.Javascript;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.util.*;
import com.codejune.http.ContentType;
import com.codejune.http.Header;
import com.codejune.http.HttpResponse;
import com.codejune.http.Type;
import com.codejune.tool.CloudStorageDownload;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 蓝奏云
 *
 * @author ZJ
 * */
public final class LanznCloudStorageDownload extends CloudStorageDownload {

    @Override
    public String getDirectUrl(String url) {
        Exception error = null;
        for (int i = 0; i < 5; i++) {
            try {
                return this.baseGetDirectUrl(url);
            } catch (Exception e) {
                error = e;
            }
        }
        throw new BaseException(error);
    }

    private String baseGetDirectUrl(String baseUrl) {
        // 获取cookie
        String baseCookie = this.getDownloadCookie(new Http(baseUrl, Type.GET)
                .addUserAgent()
                .send());

        // 获取第一层地址
        String srcUrl = new Http(baseUrl, Type.GET)
                .addUserAgent()
                .addHeader("cookie", baseCookie)
                .send()
                .getBody();

        for (String item : RegexUtil.find("src=\"(.*?)\"", srcUrl)) {
            if (item.startsWith("/fn")) {
                srcUrl = item;
                break;
            }
        }

        // 获取签名信息
        String signResult = new Http("https://wwvx.lanzoul.com" + srcUrl, Type.GET)
                .addUserAgent()
                .send()
                .getBody();
        String wp_sign = RegexUtil.find("var wp_sign = '(.*?)';", signResult, 1);
        String ajaxdata = RegexUtil.find("var ajaxdata = '(.*?)';", signResult, 1);
        String kdns = RegexUtil.find("var kdns =(.*?);", signResult, 1);
        String url = RegexUtil.find("url : '(.*?)',", signResult, 1);

        if (StringUtil.isEmpty(url)) {
            throw new BaseException("获取失败，请重试");
        }

        // 获取实际下载地址
        String originResult = new Http("https://wwvx.lanzoul.com" + url, Type.POST)
                .setContentType(ContentType.FORM_URLENCODED)
                .addUserAgent()
                .addHeader("referer", "https://wwvx.lanzoul.com/fn?")
                .setBody("action=downprocess&websignkey=" + ajaxdata + "&signs=" + ajaxdata + "&sign=" + wp_sign + "&websign=&kd=" + kdns + "&ves=1")
                .send()
                .getBody();
        Json originResultJson = Json.parse(originResult);

        // 下载地址
        String downloadUrl = originResultJson.get("dom", String.class) + "/file/" + originResultJson.get("url", String.class);

        // 直接获取
        HttpResponse<String> httpResponseResult = new Http(downloadUrl, Type.GET)
                .addUserAgent()
                .send();
        if (httpResponseResult.getCode() == 302) {
            Header location = httpResponseResult.getHeader("Location");
            if (location != null) {
                return location.getValue();
            }
        }

        // 获取cookie
        String cookie = this.getDownloadCookie(httpResponseResult);
        httpResponseResult = new Http(downloadUrl, Type.GET)
                .addUserAgent()
                .addHeader("cookie", cookie)
                .send();

        if (httpResponseResult.getCode() != 302) {
            throw new BaseException("获取失败");
        }

        Header location = httpResponseResult.getHeader("Location");
        if (location == null) {
            throw new BaseException("location is null");
        }
        return location.getValue();
    }

    private String getDownloadCookie(HttpResponse<String> httpResponseResult) {
        Map<String, String> cookie = new LinkedHashMap<>();
        for (Header header : httpResponseResult.getHeaderList("Set-Cookie")) {
            String value = header.getValue().split(";")[0];
            cookie.put(value.split("=")[0], value.split("=")[1]);
        }
        String body = httpResponseResult.getBody();
        body = RegexUtil.find("<script>(.*?)</script>", body, 1);
        Javascript javascript = new Javascript("let document = {\n" +
                "    cookie: \"\",\n" +
                "    location: {\n" +
                "        reload: () => {}\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "let location = {\n" +
                "    host: \"developer-oss.lanrar.com\"\n" +
                "};\n" +
                "\n" +
                body +
                "\n" +
                "let test = () => {\n" +
                "    return document.cookie;\n" +
                "};");
        String execute = javascript.execute("test");
        if (execute == null) {
            execute = "";
        }
        execute = execute.split(";")[0];
        cookie.put(execute.split("=")[0], execute.split("=")[1]);
        String result = "";
        for (String key : cookie.keySet()) {
            result = StringUtil.append(result, key, "=", cookie.get(key), "; ");
        }
        if (!StringUtil.isEmpty(result)) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

}