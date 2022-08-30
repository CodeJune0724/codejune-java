package com.codejune.common;

import com.codejune.common.exception.ErrorException;

/**
 * 系统类型配置
 *
 * @author ZJ
 * */
public enum SystemOS {

    /**
     * windows
     * */
    WINDOWS("Windows"),

    /**
     * linux
     * */
    LINUX("Linux");

    /**
     * osName
     * */
    private final String osName;

    /**
     * 构造函数
     *
     * @param osName osName
     * */
    SystemOS(String osName) {
        this.osName = osName;
    }

    /**
     * 获取当前系统类型
     *
     * @return 当前系统类型
     * */
    public static SystemOS getCurrentSystemOS() {
        String osName = System.getProperty("os.name");
        for (SystemOS systemOS : SystemOS.values()) {
            if (osName.contains(systemOS.osName)) {
                return systemOS;
            }
        }
        throw new ErrorException("未找到系统类型");
    }

}