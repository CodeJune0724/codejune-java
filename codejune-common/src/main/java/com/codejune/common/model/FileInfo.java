package com.codejune.common.model;

import com.codejune.common.exception.InfoException;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * 文件信息
 *
 * @author ZJ
 * */
public abstract class FileInfo {

    private final boolean isFile;

    private final boolean isDir;

    private final String path;

    private final String name;

    private final Date updateTime;

    public FileInfo(boolean isFile, String path, String name, Date updateTime) {
        this.isFile = isFile;
        this.isDir = !isFile;
        this.path = path;
        this.name = name;
        this.updateTime = updateTime;
    }

    public FileInfo(File file) {
        if (file == null) {
            throw new InfoException("file is null");
        }
        if (!file.exists()) {
            throw new InfoException("file is null");
        }

        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        cal.setTimeInMillis(time);
        Date date = cal.getTime();
        this.isFile = file.isFile();
        this.isDir = file.isDirectory();
        this.path = file.getParent();
        this.name = file.getName();
        this.updateTime = date;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isDir() {
        return isDir;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 获取大小
     *
     * @return 大小
     * */
    public abstract long getSize();

    /**
     * 获取文件数据
     *
     * @return 数据
     * */
    public String getData() {
        return null;
    }

    /**
     * 获取完整路径
     *
     * @return 完整路径
     * */
    public final String getAbsolutePath() {
        return new File(path, name).getAbsolutePath();
    }

}