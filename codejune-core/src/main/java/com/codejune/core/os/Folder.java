package com.codejune.core.os;

import com.codejune.core.BaseException;
import com.codejune.core.util.StringUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 文件夹
 *
 * @author ZJ
 * */
public final class Folder implements FileInfo {

    private String path;

    public Folder(String path) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new BaseException("非文件夹");
            }
        } else {
            if (!file.mkdirs()) {
                throw new BaseException("创建文件夹失败");
            }
        }
        this.path = path;
    }

    public Folder(String parent, String name) {
        this(new java.io.File(parent, name).getAbsolutePath());
    }

    @Override
    public String getName() {
        return new java.io.File(path).getName();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Date getUpdateTime() {
        Calendar calendar = Calendar.getInstance();
        long time = new java.io.File(path).lastModified();
        calendar.setTimeInMillis(time);
        return calendar.getTime();
    }

    @Override
    public long getSize() {
        long result = 0;
        for (FileInfo fileInfo :getChildren()) {
            result = result + fileInfo.getSize();
        }
        return result;
    }

    /**
     * 获取所有文件夹
     *
     * @return 所有文件夹
     * */
    public List<Folder> getFolder() {
        List<Folder> result = new ArrayList<>();
        java.io.File file = new java.io.File(this.path);
        java.io.File[] files = file.listFiles();
        if (files == null) {
            return result;
        }
        for (java.io.File item : files) {
            if (item.isDirectory()) {
                result.add(new Folder(item.getAbsolutePath()));
            }
        }
        return result;
    }

    /**
     * 获取所有文件
     *
     * @return 所有文件
     * */
    public List<File> getFile() {
        List<File> result = new ArrayList<>();
        java.io.File file = new java.io.File(this.path);
        java.io.File[] files = file.listFiles();
        if (files == null) {
            return result;
        }
        for (java.io.File item : files) {
            if (item.isFile()) {
                result.add(new File(item));
            }
        }
        return result;
    }

    /**
     * 获取子文件
     *
     * @return 子文件集合
     * */
    public List<FileInfo> getChildren() {
        List<FileInfo> result = new ArrayList<>();
        result.addAll(this.getFolder());
        result.addAll(this.getFile());
        return result;
    }

    /**
     * 删除
     * */
    public void delete() {
        for (File file : this.getFile()) {
            file.delete();
        }
        for (Folder folder : this.getFolder()) {
            folder.delete();
        }
        if (!new java.io.File(this.path).delete()) {
            throw new BaseException("删除文件夹失败");
        }
    }

    /**
     * 父级
     *
     * @return 父级
     * */
    public Folder parent() {
        return new Folder(new java.io.File(this.path).getParent());
    }

    /**
     * 复制
     *
     * @param copyPath 复制路径
     * @param newName 新文件夹名
     *
     * @return File
     * */
    public Folder copy(String copyPath, String newName) {
        if (StringUtil.isEmpty(copyPath)) {
            return null;
        }
        if (StringUtil.isEmpty(newName)) {
            newName = getName();
        }
        Folder result = new Folder(new java.io.File(copyPath, newName).getAbsolutePath());
        for (File file : this.getFile()) {
            file.copy(result.getPath());
        }
        for (Folder folder : this.getFolder()) {
            folder.copy(result.getPath());
        }
        return result;
    }

    /**
     * 复制
     *
     * @param copyPath 复制路径
     *
     * @return File
     * */
    public Folder copy(String copyPath) {
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
        java.io.File file = new java.io.File(this.getPath());
        java.io.File newFile = new java.io.File(file.getParent(), name);
        if (!file.renameTo(newFile)) {
            throw new BaseException("重命名失败");
        }
        this.path = newFile.getAbsolutePath();
    }

}