package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.common.handler.DownloadHandler;
import com.codejune.common.model.FileInfo;
import com.codejune.common.util.ArrayUtil;
import java.io.File;
import java.util.List;

/**
 * Ftp
 *
 * @author ZJ
 * */
public abstract class Ftp implements Closeable {

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    protected Ftp(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.connect();
    }

    /**
     * 连接
     * */
    protected abstract void connect();

    /**
     * 关闭
     * */
    @Override
    public abstract void close();

    /**
     * 上传内容
     *
     * @param path 上传路径
     * @param name 上传文件名
     * @param data 内容
     */
    public abstract void upload(String path, String name, String data);

    /**
     * 上传内容
     *
     * @param path 上传路径
     * @param name 上传文件名
     * @param file file
     */
    public abstract void upload(String path, String name, File file);

    /**
     * 获取文件信息
     *
     * @param path 路径
     *
     * @return List
     * */
    protected abstract List<FileInfo> baseLs(String path);

    /**
     * 获取文件信息
     *
     * @param path 路径
     *
     * @return List
     * */
    public List<FileInfo> ls(String path) {
        List<FileInfo> result = baseLs(path);
        int size = result.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                FileInfo fileInfoI = result.get(i);
                FileInfo fileInfoJ = result.get(j);
                if (fileInfoI.getUpdateTime().getTime() < fileInfoJ.getUpdateTime().getTime()) {
                    ArrayUtil.move(result, j, i);
                }
            }
        }
        return result;
    }

    /**
     * 下载文件
     *
     * @param path 目录
     * @param fileName 文件名
     * @param downloadHandler 下载处理
     * */
    public abstract void download(String path, String fileName, DownloadHandler downloadHandler);

    /**
     * 是否还在连接中
     *
     * @return 是否还在连接中
     * */
    public abstract boolean isConnected();

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}