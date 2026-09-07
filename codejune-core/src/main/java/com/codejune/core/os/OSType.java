package com.codejune.core.os;

import com.codejune.core.BaseException;
import com.codejune.core.util.ShellUtil;
import com.codejune.core.util.StringUtil;

import java.lang.System;

/**
 * 系统类型
 *
 * @author ZJ
 * */
public enum OSType {

    WINDOWS_11,

    WINDOWS_10,

    WINDOWS_8,

    WINDOWS_7,

    LINUX;

    /**
     * 是否是windows
     *
     * @return 当前系统类型
     * */
    public boolean isWindows() {
        return this.name().startsWith("WINDOWS");
    }

    /**
     * 获取当前系统类型
     *
     * @return 当前系统类型
     * */
    public static OSType getCurrentOSType() {
        String osName = System.getProperty("os.name");
        if (osName.contains("11")) {
            return WINDOWS_11;
        }
        if (osName.contains("10")) {
            return WINDOWS_10;
        }
        if (osName.contains("8")) {
            return WINDOWS_8;
        }
        if (osName.contains("7")) {
            return WINDOWS_7;
        }
        if (osName.contains("Linux")) {
            return LINUX;
        }
        String ver = ShellUtil.fastCommand("ver");
        if (!StringUtil.isEmpty(ver) && ver.contains("Windows")) {
            return WINDOWS_11;
        }
        throw new BaseException("未找到系统类型");
    }

}