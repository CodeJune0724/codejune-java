package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.common.Listener;
import com.codejune.common.os.FileInfo;
import com.codejune.common.util.ArrayUtil;
import java.io.InputStream;
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

    /**
     * 是否还在连接中
     *
     * @return 是否还在连接中
     * */
    public abstract boolean isConnected();

    /**
     * 路径是否存在
     *
     * @param path path
     *
     * @return 路径是否存在
     * */
    public abstract boolean exist(String path);

    /**
     * 是否是文件
     *
     * @param path path
     *
     * @return 是否是文件
     * */
    public abstract boolean isFile(String path);

    /**
     * 是否是文件夹
     *
     * @param path path
     *
     * @return 是否是文件夹
     * */
    public abstract boolean isFolder(String path);

    /**
     * 获取文件信息
     *
     * @param path 路径
     *
     * @return List
     * */
    public final List<FileInfo> ls(String path) {
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
     * 上传
     *
     * @param path 上传路径
     * @param updateFileName 上传文件名
     * @param inputStream inputStream
     */
    public abstract void upload(String path, String updateFileName, InputStream inputStream);

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @param listener listener
     * */
    public abstract void download(String filePath, Listener<InputStream> listener);

    /**
     * 删除
     *
     * @param path 路径
     * */
    public abstract void delete(String path);

    /**
     * 创建文件夹
     *
     * @param path path
     * */
    public abstract void createFolder(String path);

    /**
     * 关闭
     * */
    @Override
    public abstract void close();

    /**
     * 连接
     * */
    protected abstract void connect();

    /**
     * 获取文件信息
     *
     * @param path 路径
     *
     * @return List
     * */
    protected abstract List<FileInfo> baseLs(String path);

}