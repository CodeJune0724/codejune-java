package com.codejune.tool.cloudstoragedownload;

import com.codejune.Http;
import com.codejune.Javascript;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.util.*;
import com.codejune.http.*;
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
        return this.baseGetDirectUrl(url);
    }

    private String baseGetDirectUrl(String baseUrl) {
        String host = RegexUtil.find("https:\\/\\/(.+)\\/", baseUrl, 1);

        // 获取cookie
        String baseCookie = this.getDownloadCookie(new Http(new HttpRequest(baseUrl, Type.GET).addUserAgent().addHeader("host", host)).send());

        // 获取第一层地址
        HttpResponse<String> httpResponse = new Http(new HttpRequest(baseUrl, Type.GET).addHeader("cookie", baseCookie).addUserAgent().addHeader("host", host)).send();
        String srcUrl = httpResponse.getBody();
        for (String item : RegexUtil.find("src=\"(.*?)\"", srcUrl)) {
            if (item.startsWith("/fn")) {
                srcUrl = item;
                break;
            }
        }

        // 另外2个cookie
        baseCookie = baseCookie + "; " + ArrayUtil.toString(httpResponse.getHeaderList("set-cookie"), header -> header.getValue().split("; ")[0], "; ");

        // 获取签名信息
        String signResult = new Http(new HttpRequest("https://" + host + srcUrl, Type.GET).addHeader("cookie", baseCookie).addUserAgent())
                .send()
                .getBody();
        String wp_sign = RegexUtil.find("var wp_sign = '(.*?)';", signResult, 1);
        String ajaxdata = RegexUtil.find("var ajaxdata = '(.*?)';", signResult, 1);
        String kdns = RegexUtil.find("var kdns =(.*?);", signResult, 1);
        if (!new Http("https://developer4.lanrar.com/file/kdns.js", Type.GET).send().getBody().contains("true")) {
            kdns = "0";
        }
        String url = RegexUtil.find("url : '(.+)',", signResult, 1);
        if (StringUtil.isEmpty(url)) {
            throw new BaseException("获取失败，请重试");
        }

        // 获取实际下载地址
        String originResult = new Http(new HttpRequest("https://" + host + url, Type.POST)
                .setContentType(ContentType.FORM_URLENCODED)
                .addHeader("referer", "https://" + host + "/fn?")
                .setBody("action=downprocess&websignkey=" + ajaxdata + "&signs=" + ajaxdata + "&sign=" + wp_sign + "&websign=&kd=" + kdns + "&ves=1")
                .addUserAgent()
                .addHeader("cookie", baseCookie)
        )
                .send()
                .getBody();
        Json originResultJson = Json.parse(originResult);

        // 下载地址
        String dom = originResultJson.get("dom", String.class);
        if ("0".equals(dom)) {
            dom = "https://developer2oss.lanzouc.com:661";
        }
        String downloadUrl = dom + "/file/" + originResultJson.get("url", String.class);

        // 直接获取
        HttpResponse<String> httpResponseResult = new Http(new HttpRequest(downloadUrl, Type.GET).addUserAgent())
                .send();
        if (httpResponseResult.getCode() == 302) {
            Header location = httpResponseResult.getHeader("Location");
            if (location != null) {
                return location.getValue();
            }
        }

        // 获取cookie
        String cookie = this.getDownloadCookie(httpResponseResult);
        httpResponseResult = new Http(new HttpRequest(downloadUrl, Type.GET).addHeader("cookie", cookie).addUserAgent()).send();

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
        if (!StringUtil.isEmpty(execute)) {
            execute = execute.split(";")[0];
            cookie.put(execute.split("=")[0], execute.split("=")[1]);
        }
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