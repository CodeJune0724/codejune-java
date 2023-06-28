package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.io.writer.OutputStreamWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = IOUtil.getInputStream(file);
            outputStream = httpServletResponse.getOutputStream();
            httpServletResponse.setHeader("Content-Length", String.valueOf(inputStream.available()));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(inputStream);
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
        InputStream inputStream = null;
        try {
            inputStream = httpServletRequest.getInputStream();
            TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
            return textInputStreamReader.getData();
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 转换文件
     *
     * @param multipartFile multipartFile
     * @param path 输出路径
     *
     * @return file
     * */
    public static File parseFile(MultipartFile multipartFile, String path) {
        if (multipartFile == null) {
            throw new InfoException("multipartFile is null");
        }
        if (path == null) {
            throw new InfoException("path is null");
        }
        if (StringUtil.isEmpty(multipartFile.getOriginalFilename())) {
            throw new InfoException("multipartFile.getOriginalFilename() is null");
        }
        File result = new File(path, multipartFile.getOriginalFilename());
        try (InputStream inputStream = multipartFile.getInputStream()) {
            new com.codejune.common.os.File(result).write(inputStream);
        } catch (Exception e) {
            throw new InfoException(e);
        }
        return result;
    }

}