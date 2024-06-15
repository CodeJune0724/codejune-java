package com.codejune.core.util;

/**
 * StringUtil
 *
 * @author ZJ
 * */
public final class StringUtil {

    /**
     * 是否为空
     *
     * @param s s
     *
     * @return 空返回true
     * */
    public static boolean isEmpty(Object s) {
        return s == null || "".equals(s.toString()) || s.toString().replaceAll(" ", "").isEmpty();
    }

    /**
     * 连接字符串
     *
     * @param strings 字符串数组
     *
     * @return 连接后的字符串
     * */
    public static String append(String... strings) {
        StringBuilder result = new StringBuilder();
        for (String s : strings) {
            if (s == null) {
                s = "";
            }

            result.append(s);
        }
        return result.toString();
    }

    /**
     * 驼峰转下划线
     *
     * @param data 转换前的驼峰式命名的字符串
     *
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String humpToUnderline(String data) {
        StringBuilder result = new StringBuilder();
        if (data != null && !data.isEmpty()) {
            result.append(data.substring(0, 1).toUpperCase());
            for (int i = 1; i < data.length(); i++) {
                String s = data.substring(i, i + 1);
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                result.append(s.toUpperCase());
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     *
     * @param data data
     *
     * @return data
     * */
    public static String underlineToHump(String data) {
        if (isEmpty(data)) {
            return data;
        }
        StringBuilder result = new StringBuilder();
        String[] split = data.split("_");
        for (int i = 0; i < split.length; i++) {
            String splitItem = split[i];
            if (isEmpty(splitItem)) {
                continue;
            }
            for (int j = 0; j < splitItem.length(); j++) {
                String s = splitItem.substring(j, j + 1);
                if (i == 0 || j != 0) {
                    s = s.toLowerCase();
                }
                result.append(s);
            }
        }
        return result.toString();
    }

}