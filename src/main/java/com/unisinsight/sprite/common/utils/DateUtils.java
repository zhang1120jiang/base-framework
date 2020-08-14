package com.unisinsight.sprite.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * ClassName：DateUtils
 * Description:日期转换工具类
 * author: mayouwen
 * date: 2018/12/3
 */
@Slf4j
public final class DateUtils {

    private DateUtils() {
    }

    /**
     * 字符创转日期
     * @param str
     * @return Date
     */
    public static Date stringToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        if (str != null) {
            try {
                date = sdf.parse(str);
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
        }
        return date;
    }

    /**
     * 字符创转日期
     * @param str
     * @return Date
     */
    public static Date stringsToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        if (str != null) {
            try {
                date = sdf.parse(str);
            } catch (ParseException e) {
                log.error("stringsToDate:" + str);
                log.error(e.getMessage());
            }
        }
        return date;
    }

    /**
     * 日期转字符串
     * @param date
     * @return string
     */
    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = null;
        if (date != null) {
            s = sdf.format(date);
        }
        return s;
    }

    /**
     * 日期转字符串
     * @param date
     * @return string
     */
    public static String dateToStrings(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = null;
        if (date != null) {
            s = sdf.format(date);
        }
        return s;
    }

    /**
     * 获取前一天日期
     */
    public static Date getDateBefore(){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,-1);
        return calendar.getTime();
    }

    /** 获取day天之前的日期
     * @param day
     * @return
     */
    public static Date getDaysBefore(Integer day){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,0-day);
        return calendar.getTime();
    }

    /**
     * 日期格式校验
     * @param str 日期字符串
     * @param dateFormat 日期格式字符串
     * @return 校验结果
     */
    public static boolean isValidDate(String str, String dateFormat) {
        boolean convertSuccess=true;
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        try {
            //这种情况下java不会把你输入的日期进行计算，比如55个月那么就是不合法的日期了，直接异常
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess=false;
        }
        return convertSuccess;
    }

    /**
     * 获取上周开始时间
     * @return
     */
    public static Date getBeginDayOfLastWeek() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DATE, 2 - dayofweek - 7);
        return cal.getTime();
    }

    /**
     * 判断当前日期是周几
     * @return
     */
    public static Integer getweek(Date date) {
        Integer[] weeks = {0, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekIndex < 0) {
            weekIndex = 0;
        }
        return weeks[weekIndex];
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
     * 日期转字符串
     *
     * @param date
     * @return string
     */
    public static String dateToString(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
