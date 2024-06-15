package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DateUtil
 *
 * @author ZJ
 * */
public final class DateUtil {

    /**
     * 默认日期
     * */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 兼容的日期
     * */
    public static final List<String> COMPATIBLE_DATES = new ArrayList<>();

    static {
        COMPATIBLE_DATES.add("yyyy-MM-dd HH:mm:ss.0");
        COMPATIBLE_DATES.add(DEFAULT_DATE_FORMAT);
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @param pattern 日期格式
     *
     * @return 所指定格式的日期
     * */
    public static String format(Date date, String pattern) {
        if (date == null || StringUtil.isEmpty(pattern)) {
            return null;
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 转换成date
     *
     * @param date 日期
     * @param pattern 日期格式
     * @param locale locale
     *
     * @return Date
     * */
    public static Date parse(String date, String pattern, Locale locale) {
        if (StringUtil.isEmpty(date) || StringUtil.isEmpty(pattern)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat;
        if (locale == null) {
            simpleDateFormat = new SimpleDateFormat(pattern);
        } else {
            simpleDateFormat = new SimpleDateFormat(pattern, locale);
        }
        try {
            return simpleDateFormat.parse(date);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 转换成date
     *
     * @param date 日期
     * @param pattern 日期格式
     *
     * @return Date
     * */
    public static Date parse(String date, String pattern) {
        return parse(date, pattern, null);
    }

}