package com.codejune.core.os;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.io.*;
import java.time.LocalDateTime;

/**
 * 文件
 *
 * @author ZJ
 * */
public final class File implements FileInfo<String> {

    private java.io.File file;

    public File(java.io.File file) {
        if (file == null) {
            throw new BaseException("file is null");
        }
        try {
            if (file.exists()) {
                if (!file.isFile()) {
                    throw new BaseException("非文件");
                }
            } else {
                new Folder(file.getParent());
                if (!file.createNewFile()) {
                    throw new BaseException("创建文件失败");
                }
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.file = file;
    }

    public File(String path) {
        this(new java.io.File(path));
    }

    public File(String parent, String name) {
        this(new java.io.File(parent, name));
    }

    @Override
    public String getName() {
        return this.file.getName();
    }

    @Override
    public String getPath() {
        return this.file.getAbsolutePath();
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return ObjectUtil.parse(this.file.lastModified(), LocalDateTime.class);
    }

    @Override
    public long getSize() {
        return this.file.length();
    }

    /**
     * 获取数据
     *
     * @return 文件数据
     * */
    @Override
    public String getData() {
        try (TextInputStreamReader textInputStreamReader = new TextInputStreamReader(IOUtil.getInputStream(this.file))) {
            return textInputStreamReader.getData();
        }
    }

    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * 删除
     * */
    public void delete() {
        if (!file.delete()) {
            throw new BaseException("删除文件失败");
        }
    }

    /**
     * 父级文件夹
     *
     * @return 父级
     * */
    public Folder parent() {
        return new Folder(this.file.getParentFile().getAbsolutePath());
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * @param append 追加
     * */
    public void write(byte[] bytes, boolean append) {
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(bytes);
        }
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * */
    public void write(byte[] bytes) {
        this.write(bytes, false);
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * @param append 追加
     * */
    public void write(InputStream inputStream, boolean append) {
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        this.write(inputStream, false);
    }

    /**
     * 写入数据
     *
     * @param data data
     * @param append 追加
     * */
    public void write(String data, boolean append) {
        if (data == null) {
            return;
        }
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(data.getBytes());
        }
    }

    /**
     * 写入数据
     *
     * @param data data
     * */
    public void write(String data) {
        this.write(data, false);
    }

    /**
     * 复制文件
     *
     * @param copyPath 复制路径
     * @param fileName 文件名
     *
     * @return File
     * */
    public File copy(String copyPath, String fileName) {
        if (StringUtil.isEmpty(copyPath)) {
            return null;
        }
        if (StringUtil.isEmpty(fileName)) {
            fileName = getName();
        }
        new Folder(copyPath);
        java.io.File copyFile = new java.io.File(copyPath, fileName);
        if (copyFile.exists()) {
            new File(copyFile).delete();
        }
        File result = new File(copyFile);
        try (InputStream inputStream = IOUtil.getInputStream(file)) {
            result.write(inputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return result;
    }

    /**
     * 复制文件
     *
     * @param copyPath 复制路径
     *
     * @return File
     * */
    public File copy(String copyPath) {
        return copy(copyPath, null);
    }

    /**
     * 重命名
     *
     * @param name 新名称
     * */
    public void rename(String name) {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        java.io.File newFile = new java.io.File(this.file.getParent(), name);
        if (!this.file.renameTo(newFile)) {
            throw new BaseException("重命名失败");
        }
        this.file = newFile.getAbsoluteFile();
    }

    /**
     * 获取后缀
     *
     * @return 后缀
     * */
    public String getSuffix() {
        return this.getName().split("\\.")[1];
    }

}