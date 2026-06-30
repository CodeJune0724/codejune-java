package com.codejune.core.util;

/**
 * ConsoleUtil
 *
 * @author ZJ
 * */
public final class ConsoleUtil {

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

}