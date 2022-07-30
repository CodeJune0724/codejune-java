package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * ServletUtil
 *
 * @author ZJ
 * */
public final class ServletUtil {

    /**
     * 下载文件
     *
     * @param httpServletResponse httpServletResponse
     * @param file file
     * */
    public static void download(HttpServletResponse httpServletResponse, File file) {
        if (file == null) {
            return;
        }
        if (!file.isFile()) {
            throw new InfoException("非文件");
        }

        try {
            httpServletResponse.setContentType("application/x-download");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = Files.newInputStream(file.toPath());
            httpServletResponse.setHeader("Content-Length", String.valueOf(inputStream.available()));
            outputStream = httpServletResponse.getOutputStream();
            byte[] bytes = new byte[2048];
            int read = inputStream.read(bytes);
            while (read != -1) {
                outputStream.write(bytes);
                read = inputStream.read(bytes);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(outputStream);
        }
    }

    /**
     * 获取请求体数据
     *
     * @param httpServletRequest httpServletRequest
     *
     * @return 请求体数据
     * */
    public static String getRequestBody(HttpServletRequest httpServletRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = httpServletRequest.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(bufferedReader);
        }
    }

}