package com.codejune.tool;

import com.codejune.Http;
import com.codejune.Javascript;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.util.RegexUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.http.ContentType;
import com.codejune.http.Header;
import com.codejune.http.HttpResponseResult;
import com.codejune.http.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 蓝奏云
 *
 * @author ZJ
 * */
public final class Lanzn {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36";

    /**
     * url转换
     *
     * @param baseUrl 基础下载地址
     *
     * @return 转换后的下载地址
     * */
    public static String urlParse(String baseUrl) {
        // 获取cookie
        String baseCookie = getDownloadCookie(new Http(baseUrl, Type.GET)
                .addHeader("user-agent", USER_AGENT)
                .send());

        // 获取第一层地址
        String srcUrl = new Http(baseUrl, Type.GET)
                .addHeader("user-agent", USER_AGENT)
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
                .addHeader("user-agent", USER_AGENT)
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
                .addHeader("user-agent", USER_AGENT)
                .addHeader("referer", "https://wwvx.lanzoul.com/fn?")
                .setBody("action=downprocess&websignkey=" + ajaxdata + "&signs=" + ajaxdata + "&sign=" + wp_sign + "&websign=&kd=" + kdns + "&ves=1")
                .send()
                .getBody();
        Json originResultJson = Json.parse(originResult);

        // 下载地址
        String downloadUrl = originResultJson.get("dom", String.class) + "/file/" + originResultJson.get("url", String.class);

        // 直接获取
        HttpResponseResult<String> httpResponseResult = new Http(downloadUrl, Type.GET)
                .addHeader("user-agent", USER_AGENT)
                .send();
        if (httpResponseResult.getCode() == 302) {
            Header location = httpResponseResult.getHeader("Location");
            if (location != null) {
                return location.getValue();
            }
        }

        // 获取cookie
        String cookie = getDownloadCookie(httpResponseResult);
        httpResponseResult = new Http(downloadUrl, Type.GET)
                .addHeader("user-agent", USER_AGENT)
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

    private static String getDownloadCookie(HttpResponseResult<String> httpResponseResult) {
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