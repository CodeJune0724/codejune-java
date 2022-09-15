package com.codejune.common.util;

import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.DateType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
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
            throw new InfoException(e.getMessage());
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

    /**
     * 将指定格式日期转成毫秒
     *
     * @param date 日期
     * @param dateType 日期类型
     *
     * @return 对应的毫秒数
     * */
    public static long transformMillisecond(long date, DateType dateType) {
        if (date == 0 || dateType == null) {
            return 0L;
        }
        switch (dateType) {
            case YEAR:
                return transformMillisecond(date * 365, DateType.DAY);
            case MONTH:
                return transformMillisecond(date * 30, DateType.DAY);
            case week:
                return transformMillisecond(date * 7, DateType.DAY);
            case DAY:
                return transformMillisecond(date * 24, DateType.HOUR);
            case HOUR:
                return transformMillisecond(date * 60, DateType.MINUTE);
            case MINUTE:
                return transformMillisecond(date * 60, DateType.SECOND);
            case SECOND:
                return transformMillisecond(date * 1000, DateType.MILLISECOND);
            case MILLISECOND:
                return date;
            default:
                throw new ErrorException("dateType未配置");
        }
    }

}