package com.codejune.tool.cloudstoragedownload;

import com.codejune.Http;
import com.codejune.Javascript;
import com.codejune.Json;
import com.codejune.core.BaseException;
import com.codejune.core.Range;
import com.codejune.core.io.Reader;
import com.codejune.core.io.reader.FileReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.*;
import com.codejune.http.ContentType;
import com.codejune.http.Header;
import com.codejune.http.HttpResponse;
import com.codejune.http.Type;
import com.codejune.tool.CloudStorageDownload;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * 文件分割
     *
     * @param file file
     * @param outPath 输出目录
     *
     * @return 分割后的文件
     * */
    public List<File> fileSplit(File file, String outPath) {
        if (!FileUtil.isFile(file)) {
            throw new BaseException("文件不存在");
        }
        long splitSize = 90 * 1024 * 1024;
        long size = new com.codejune.core.os.File(file).getSize();
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        List<File> result = ArrayUtil.asList();
        try (FileReader fileReader = new FileReader(file)) {
            for (long i = 0; i < size; i = i + splitSize) {
                long end = i + splitSize;
                if (end > size) {
                    end = size;
                }
                File spliFile = new File(outPath, fileName + "." + (i / splitSize) + "." + suffix);
                fileReader.read(byteBuffer -> {
                    byte[] aByte = Reader.getByte(byteBuffer);
                    new com.codejune.core.os.File(spliFile).write(new ByteArrayInputStream(aByte), true);
                }, new Range(i, end));
                result.add(spliFile);
            }
        }
        return result;
    }

    /**
     * 文件合并
     *
     * @param fileList fileList
     * @param outPath 输出目录
     *
     * @return 合并后的文件
     * */
    public File fileMerge(List<File> fileList, String outPath) {
        if (ObjectUtil.isEmpty(fileList)) {
            return null;
        }
        String suffix = fileList.getFirst().getName().substring(fileList.getFirst().getName().lastIndexOf(".") + 1);
        String fileName = fileList.getFirst().getName().substring(0, fileList.getFirst().getName().indexOf(".0." + suffix));
        if (StringUtil.isEmpty(fileName)) {
            throw new BaseException("文件错误");
        }
        File result = new File(outPath, fileName + "." + suffix);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(IOUtil.getOutputStream(result, true))) {
            for (File file : fileList) {
                try (FileReader fileReader = new FileReader(file)) {
                    fileReader.read(outputStreamWriter::write);
                }
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return result;
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