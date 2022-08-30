package com.codejune.common.os;

import java.util.Date;

/**
 * 文件信息
 *
 * @author ZJ
 * */
public interface FileInfo {

    /**
     * 获取文件名
     *
     * @return 文件名
     * */
    String name();

    /**
     * 获取路径
     *
     * @return 路径
     * */
    String path();

    /**
     * 获取更新时间
     *
     * @return 更新时间
     * */
    Date getUpdateTime();

    /**
     * 文件大小
     *
     * @return 文件大小
     * */
    long getSize();

}