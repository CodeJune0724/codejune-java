package com.codejune.core.os;

import java.time.LocalDateTime;

/**
 * 文件信息
 *
 * @author ZJ
 * */
public interface FileInfo<DATA> {

    /**
     * 获取文件名
     *
     * @return 文件名
     * */
    String getName();

    /**
     * 获取路径
     *
     * @return 路径
     * */
    String getPath();

    /**
     * 获取更新时间
     *
     * @return 更新时间
     * */
    LocalDateTime getUpdateTime();

    /**
     * 文件大小
     *
     * @return 文件大小
     * */
    long getSize();

    /**
     * 获取数据
     *
     * @return DATA
     * */
    DATA getData();

    /**
     * 是否是文件
     *
     * @return 是否是文件
     * */
    boolean isFile();

}