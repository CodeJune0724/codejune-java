package com.codejune.tool;

import com.codejune.Http;
import com.codejune.core.BaseException;
import com.codejune.core.Progress;
import com.codejune.core.io.Reader;
import com.codejune.core.io.reader.InputStreamReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.http.Type;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 网盘下载器
 *
 * @author ZJ
 * */
public abstract class CloudStorageDownload {

    /**
     * 获取直链
     *
     * @param url 原url
     *
     * @return 直链
     * */
    public abstract String getDirectUrl(String url);

    /**
     * 下载文件
     *
     * @param url 原url
     * @param savePath 保存的目录
     * @param progress 进度
     *
     * @return 下载的文件
     * */
    public final File download(String url, String savePath, Consumer<Double> progress) {
        if (StringUtil.isEmpty(savePath)) {
            throw new BaseException("savePath is null");
        }
        AtomicReference<File> result = new AtomicReference<>();
        new Http(this.getDirectUrl(url), Type.GET).addUserAgent().send(httpResponseResult -> {
            result.set(new File(savePath, httpResponseResult.getDownloadFileName()));
            Long size = httpResponseResult.getContentLength();
            Progress progressEntity;
            if (progress != null) {
                progressEntity = new Progress(size == null ? 0 : size) {
                    @Override
                    public void listen(Progress progressEntity1) {
                        progress.accept(progressEntity1.getPercentage());
                    }
                };
            } else {
                progressEntity = null;
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(httpResponseResult.getBody());
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(IOUtil.getOutputStream(result.get()))
            ) {
                inputStreamReader.read(byteBuffer -> {
                    byte[] aByte = Reader.getByte(byteBuffer);
                    outputStreamWriter.write(aByte);
                    if (progressEntity != null) {
                        progressEntity.countDown(aByte.length);
                    }
                });
            }
        });
        return result.get();
    }

    /**
     * 下载文件
     *
     * @param url 原url
     * @param savePath 保存的目录
     *
     * @return 下载的文件
     * */
    public final File download(String url, String savePath) {
        return this.download(url, savePath, null);
    }

}