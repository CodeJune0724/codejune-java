package com.codejune.common.util;

import com.codejune.common.DataType;

/**
 * BeanUtil
 *
 * @author ZJ
 * */
public final class BeanUtil {

    /**
     * 获取getter方法名
     *
     * @param fieldName 字段名
     * @param dataType 数据类型
     *
     * @return getter方法名
     * */
    public static String getGetterMethodName(String fieldName, DataType dataType) {
        String result = handler(fieldName);
        if (StringUtil.isEmpty(result)) {
            return null;
        }
        if (dataType == DataType.BOOLEAN) {
            return "is" + result;
        } else {
            return "get" + result;
        }
    }

    /**
     * 获取setter方法名
     *
     * @param fieldName 字段名
     *
     * @return setter方法名
     * */
    public static String getSetterMethodName(String fieldName) {
        String result = handler(fieldName);
        if (StringUtil.isEmpty(result)) {
            return null;
        }
        return "set" + result;
    }

    private static String handler(String fieldName) {
        if (StringUtil.isEmpty(fieldName)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        result.append(fieldName);
        if (Character.isLowerCase(result.charAt(0))) {
            if (result.length() == 1 || !Character.isUpperCase(result.charAt(1))) {
                result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
            }
        }
        return result.toString();
    }

}