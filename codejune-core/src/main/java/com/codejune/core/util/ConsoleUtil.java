package com.codejune.core.util;

import java.io.Console;
import java.util.Scanner;

/**
 * ConsoleUtil
 *
 * @author ZJ
 * */
public final class ConsoleUtil {

    private static final Scanner SCANNER = new Scanner(System.in, System.getProperty("stdin.encoding"));

    /**
     * 清屏
     */
    public static void clean() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    /**
     * 等待输入
     *
     * @param pre pre
     *
     * @return 输入的内容
     * */
    public static String scannerNext(String pre) {
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
    public static String scannerNext() {
        return scannerNext(null);
    }

    /**
     * 隐藏输入
     *
     * @param value 前置
     *
     * @return 输入的内容
     * */
    public static String hideScannerNext(String value) {
        Console console = System.console();
        if (console == null) {
            return scannerNext(value);
        }
        return new String(console.readPassword(value));
    }

}