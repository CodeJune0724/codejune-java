package com.codejune.common.util;

import com.codejune.common.BaseException;
import com.codejune.common.io.writer.OutputStreamWriter;
import com.codejune.common.os.Folder;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil
 *
 * @author ZJ
 * */
public final class ZipUtil {

    /**
     * 压缩
     *
     * @param fileList 源文件或者文件夹
     * @param outputStream outputStream
     * */
    public static void zip(List<String> fileList, OutputStream outputStream) {
        if (ObjectUtil.isEmpty(fileList)) {
            return;
        }
        if (outputStream == null) {
            return;
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (String file : fileList) {
                zip(zipOutputStream, new File(file), "");
            }
            zipOutputStream.flush();
            zipOutputStream.finish();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 压缩
     *
     * @param fileList 源文件或者文件夹
     * @param file file
     *
     * @return file
     * */
    public static File zip(List<String> fileList, File file) {
        try (OutputStream outputStream = IOUtil.getOutputStream(file)) {
            zip(fileList, outputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return file;
    }

    private static void zip(ZipOutputStream zipOutputStream, File file, String path) {
        if (zipOutputStream == null) {
            return;
        }
        if (!FileUtil.exist(file)) {
            return;
        }
        if (StringUtil.isEmpty(path)) {
            path = "";
        }
        if (!StringUtil.isEmpty(path)) {
            path = path + "/";
        }
        if (FileUtil.isFile(file)) {
            try {
                zipOutputStream.putNextEntry(new ZipEntry(path + file.getName()));
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zipOutputStream);
                try (InputStream inputStream = IOUtil.getInputStream(file)) {
                    outputStreamWriter.write(inputStream);
                }
                zipOutputStream.closeEntry();
            } catch (Exception e) {
                throw new BaseException(e);
            }
        } else {
            File[] fileList = file.listFiles();
            if (fileList == null) {
               return;
            }
            try {
                zipOutputStream.putNextEntry(new ZipEntry(path + file.getName() + "/"));
                zipOutputStream.closeEntry();
                for (File fileListItem : fileList) {
                    zip(zipOutputStream, fileListItem, path + file.getName());
                }
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 解压
     *
     * @param zipFile 压缩包
     * @param outPath 解压目录
     * */
    public static void unzip(File zipFile, String outPath) {
        if (zipFile == null || !zipFile.exists() || outPath == null) {
            return;
        }
        new Folder(outPath);
        try (ZipFile zf = new ZipFile(zipFile, Charset.forName(System.getProperty("sun.jnu.encoding")))) {
            Enumeration<?> enumeration = zf.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                if (zipEntry.isDirectory()) {
                    new Folder(new File(outPath, zipEntry.getName()).getAbsolutePath());
                } else {
                    try (InputStream inputStream = zf.getInputStream(zipEntry)) {
                        new com.codejune.common.os.File(new File(outPath, zipEntry.getName())).write(inputStream);
                    }
                }
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}