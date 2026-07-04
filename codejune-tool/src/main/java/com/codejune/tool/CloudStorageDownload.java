package com.codejune.tool;

import com.codejune.Http;
import com.codejune.core.BaseException;
import com.codejune.core.Progress;
import com.codejune.core.Range;
import com.codejune.core.io.Reader;
import com.codejune.core.io.reader.FileReader;
import com.codejune.core.io.reader.InputStreamReader;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.util.*;
import com.codejune.http.Type;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
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

    /**
     * 文件分割
     *
     * @param file file
     * @param outPath 输出目录
     *
     * @return 分割后的文件
     * */
    public final List<File> fileSplit(File file, String outPath) {
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
    public final File fileMerge(List<File> fileList, String outPath) {
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

}