package com.codejune.common.util;

import com.codejune.common.os.File;
import com.codejune.common.os.Folder;

/**
 * FileUtil
 *
 * @author ZJ
 * */
public final class FileUtil {

    /**
     * 删除
     *
     * @param file file
     * */
    public static void delete(java.io.File file) {
        if (file == null) {
           return;
        }
        if (file.exists()) {
            if (file.isFile()) {
                new File(file).delete();
            }
            if (file.isDirectory()) {
                new Folder(file.getAbsolutePath()).delete();
            }
        }
    }

}