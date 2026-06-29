package com.codejune.core.util;

import java.util.Scanner;

/**
 * ScannerUtil
 *
 * @author ZJ
 * */
public final class ScannerUtil {

    private static final Scanner SCANNER = new Scanner(System.in, System.getProperty("stdin.encoding"));

    /**
     * 等待输入
     *
     * @param pre pre
     *
     * @return 输入的内容
     * */
    public static String next(String pre) {
        if (!StringUtil.isEmpty(pre)) {
            System.out.print(pre);
        }
        return SCANNER.nextLine();
    }

    /**
     * 等待输入
     *
     * @return 输入的内容
     * */
    public static String next() {
        return next(null);
    }

}