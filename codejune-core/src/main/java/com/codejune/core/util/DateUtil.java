package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    public static final List<String> COMPATIBLE_DATE_LIST = new ArrayList<>();

    static {
        COMPATIBLE_DATE_LIST.add("yyyy-MM-dd HH:mm:ss.0");
        COMPATIBLE_DATE_LIST.add(DEFAULT_DATE_FORMAT);
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
     * 格式化日期
     *
     * @param localDateTime localDateTime
     * @param pattern 日期格式
     *
     * @return 所指定格式的日期
     * */
    public static String format(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null || StringUtil.isEmpty(pattern)) {
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期
     *
     * @param localDate localDate
     * @param pattern 日期格式
     *
     * @return 所指定格式的日期
     * */
    public static String format(LocalDate localDate, String pattern) {
        if (localDate == null || StringUtil.isEmpty(pattern)) {
            return null;
        }
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期
     *
     * @param localTime localTime
     * @param pattern 日期格式
     *
     * @return 所指定格式的日期
     * */
    public static String format(LocalTime localTime, String pattern) {
        if (localTime == null || StringUtil.isEmpty(pattern)) {
            return null;
        }
        return localTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 转换成日期
     *
     * @param date date
     * @param pattern pattern
     * @param tClass tClass
     * @param <T> T
     *
     * @return t
     * */
    @SuppressWarnings("unchecked")
    public static <T> T parse(String date, String pattern, Class<T> tClass) {
        if (StringUtil.isEmpty(date) || StringUtil.isEmpty(pattern) || tClass == null) {
            return null;
        }
        if (tClass == Date.class) {
            try {
                return (T) new SimpleDateFormat(pattern).parse(date);
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
        if (tClass == LocalDateTime.class) {
            return (T) LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
        }
        if (tClass == LocalDate.class) {
            return (T) LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        }
        if (tClass == LocalTime.class) {
            return (T) LocalTime.parse(date, DateTimeFormatter.ofPattern(pattern));
        }
        throw new BaseException("tClass not config");
    }

}