package com.codejune.ftp.os;

import com.codejune.core.os.FileInfo;
import java.io.InputStream;

/**
 * ftpFile
 *
 * @author ZJ
 * */
public abstract class File implements FileInfo {

    /**
     * 获取流
     *
     * @return InputStream
     * */
    public abstract InputStream getInputStream();

}