package com.codejune.common.os;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.Writer;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.*;
import java.nio.file.Files;
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
            throw new InfoException("file is null");
        }
        try {
            if (file.exists()) {
                if (!file.isFile()) {
                    throw new InfoException("非文件");
                }
            } else {
                new Folder(file.getParent());
                if (!file.createNewFile()) {
                    throw new InfoException("创建文件失败");
                }
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
        this.file = file;
    }

    public File(String path) {
        this(new java.io.File(path));
    }

    @Override
    public String name() {
        return this.file.getName();
    }

    @Override
    public String path() {
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
     * 获取输入流
     *
     * @return InputStream
     * */
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(this.file.toPath());
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 获取输出流
     *
     * @return OutputStream
     * */
    public OutputStream getOutputStream(boolean append) {
        try {
            return new FileOutputStream(this.file, append);
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 获取输出流
     *
     * @return OutputStream
     * */
    public OutputStream getOutputStream() {
        return getOutputStream(false);
    }

    /**
     * 删除
     * */
    public void delete() {
        if (!file.delete()) {
            throw new InfoException("删除文件失败");
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
     * 读取数据
     *
     * @return 文件数据
     * */
    public String read() {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream();
            TextInputStreamReader textInputStreamReader = new TextInputStreamReader(inputStream);
            return textInputStreamReader.read();
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
            outputStream = getOutputStream(append);
            Writer writer = new Writer(outputStream);
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
            fileName = name();
        }
        new Folder(copyPath);
        java.io.File copyFile = new java.io.File(copyPath, fileName);
        if (copyFile.exists()) {
            new File(copyFile).delete();
        }
        File result = new File(copyFile);
        InputStream inputStream = null;
        try {
            inputStream = this.getInputStream();
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
        return  copy(copyPath, null);
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
            throw new InfoException("重命名失败");
        }
        this.file = newFile.getAbsoluteFile();
    }

}
