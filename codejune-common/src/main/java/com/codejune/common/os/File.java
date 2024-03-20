package com.codejune.common.os;

import com.codejune.common.BaseException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.io.writer.OutputStreamWriter;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * 文件
 *
 * @author ZJ
 * */
public final class File implements FileInfo {

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
    public Date getUpdateTime() {
        Calendar calendar = Calendar.getInstance();
        long time = this.file.lastModified();
        calendar.setTimeInMillis(time);
        return calendar.getTime();
    }

    @Override
    public long getSize() {
        return this.file.length();
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
     * 获取数据
     *
     * @return 文件数据
     * */
    public String getData() {
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.getInputStream(file);
            TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
            return textInputStreamReader.getData();
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * @param append 是否追加
     * */
    public void write(InputStream inputStream, boolean append) {
        OutputStream outputStream = null;
        try {
            outputStream = IOUtil.getOutputStream(file, append);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write(inputStream);
        } finally {
            IOUtil.close(outputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        write(inputStream, false);
    }

    /**
     * 写入数据
     *
     * @param data data
     * @param append 是否追加
     * */
    public void write(String data, boolean append) {
        if (StringUtil.isEmpty(data)) {
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(data.getBytes());
            write(inputStream, append);
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param data data
     * */
    public void write(String data) {
        write(data, false);
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * */
    public void write(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        InputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            write(inputStream);
        } finally {
            IOUtil.close(inputStream);
        }
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
        InputStream inputStream = null;
        try {
            inputStream = IOUtil.getInputStream(file);
            result.write(inputStream);
        } finally {
            IOUtil.close(inputStream);
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

}