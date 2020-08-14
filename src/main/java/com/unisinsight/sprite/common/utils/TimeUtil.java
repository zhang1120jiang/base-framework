package com.unisinsight.sprite.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 功能描述：
 *
 * @author: qiuweiwu [qiu.weiwu@unisinsight.com]
 * @date: 2019/3/4 15:24
 */
@Slf4j
public final class TimeUtil {
    /**
     * 构造器私有化
     */
    private TimeUtil() {
    }

    /**
     * 功能描述: 获取当天（按当前传入的时区）00:00:00所对应时刻的long型值
     *
     * @param now      精确至毫秒的时间戳
     * @param timeZone 时区，默认为北京时间GMT+8
     * @return long
     * @author qiuweiwu [qiuweiwu@unisinsight.com]
     * @date 2019/3/4 15:25
     */
    public static long getStartTimeOfDay(long now, String timeZone) {
        String tz = StringUtils.isEmpty(timeZone) ? "GMT+8" : timeZone;
        TimeZone curTimeZone = TimeZone.getTimeZone(tz);
        Calendar calendar = Calendar.getInstance(curTimeZone);
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 功能描述: 获取小时（按当前传入的时区）整点所对应时刻的long型值
     *
     * @param now      精确至毫秒的时间戳
     * @param timeZone 时区，默认为北京时间GMT+8
     * @return long
     * @author qiuweiwu [qiuweiwu@unisinsight.com]
     * @date 2019/3/4 15:25
     */
    public static long getStartTimeOfHour(long now, String timeZone) {
        String tz = StringUtils.isEmpty(timeZone) ? "GMT+8" : timeZone;
        TimeZone curTimeZone = TimeZone.getTimeZone(tz);
        Calendar calendar = Calendar.getInstance(curTimeZone);
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format  如：yyyy-MM-dd HH:mm:ss
     * @return long 时间戳精确至毫秒的时间戳
     * @author qiuweiwu [qiuweiwu@unisinsight.com]
     * @date 2019/3/4 15:25
     */
    public static Long getTimestampFromTime(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            log.error("invalid time format");
            log.error("获取失败：", e.getMessage());
        }
        return 0L;
    }

    /**
     * 功能描述: 13位时间戳转换为字符串
     *
     * @param timestamp 时间戳，13位的时间戳
     * @param format    如：yyyy-MM-dd HH:mm:ss
     * @return java.lang.String
     * @author qiuweiwu [qiuweiwu@unisinsight.com]
     * @date 2019/3/7 15:35
     */
    public static String timeStamp2DateStr(Long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(timestamp);
    }

    /**
     * 获取过去任意天内的日期数组
     *
     * @param intervals intervals天内
     * @return 日期数组
     */
    public static List<String> getDay(int intervals) {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            pastDaysList.add(getPastDate(i));
        }
        return pastDaysList;
    }

    /**
     * 一天的毫秒数
     */
    private static final long ONE_DAY_STAMP = 24 * 3600 * 1000L;

    /**
     * 获取当天推迟到之前几天的数据
     *
     * @param intervals 几天
     * @return List<Long>
     */
    public static List<Long> getDayStamps(int intervals) {
        List<Long> stamps = new ArrayList<>();
        long now = getStartTimeOfDay(System.currentTimeMillis(), null) + ONE_DAY_STAMP;
        for (int i = 0; i < intervals; i++) {
            stamps.add(now - (i * ONE_DAY_STAMP));
        }
        return stamps;
    }

    /**
     * 获取过去第几天的日期
     *
     * @param past 入参
     * @return String 返回结果
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(today);
    }

    /**
     * 获取当前时间
     * @param date
     * @return
     */
    public static String getCurTimeStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = null;
        if (date != null) {
            s = sdf.format(date);
        }
        return s;
    }

    public static Date getDate(Long date) {
        return new Date(date);
    }
}
