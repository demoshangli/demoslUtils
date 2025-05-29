package com.demoshangli.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期工具类，支持日期字符串自动格式识别，
 * 默认返回标准格式（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss），
 * 并支持自定义返回格式。
 */
public class DateUtil {

    // 紧凑日期格式：yyyyMMdd
    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    // 标准日期格式：yyyy-MM-dd
    private static final DateTimeFormatter NORMAL_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 紧凑日期时间格式：yyyyMMddHHmmss
    private static final DateTimeFormatter DATETIME_COMPACT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    // 标准日期时间格式：yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter DATETIME_NORMAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*----------------------- 私有辅助方法 -----------------------*/

    /**
     * 根据传入的日期字符串自动识别日期格式（不含时间）
     * 支持 yyyyMMdd 和 yyyy-MM-dd 两种格式
     */
    private static DateTimeFormatter resolveDateFormatter(String dateStr) {
        if (dateStr == null) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        if (dateStr.contains("-")) {
            if (dateStr.length() == 10) {
                return NORMAL_DATE;
            }
        } else {
            if (dateStr.length() == 8) {
                return COMPACT_DATE;
            }
        }
        throw new IllegalArgumentException("不支持的日期格式：" + dateStr);
    }

    /**
     * 根据传入的日期时间字符串自动识别日期时间格式
     * 支持 yyyyMMddHHmmss 和 yyyy-MM-dd HH:mm:ss 两种格式
     */
    private static DateTimeFormatter resolveDateTimeFormatter(String dateTimeStr) {
        if (dateTimeStr == null) {
            throw new IllegalArgumentException("日期时间字符串不能为空");
        }
        if (dateTimeStr.contains("-")) {
            if (dateTimeStr.length() == 19) {
                return DATETIME_NORMAL;
            }
        } else {
            if (dateTimeStr.length() == 14) {
                return DATETIME_COMPACT;
            }
        }
        throw new IllegalArgumentException("不支持的日期时间格式：" + dateTimeStr);
    }

    /**
     * 格式化 LocalDate 返回字符串
     * 默认格式：标准格式 yyyy-MM-dd
     */
    private static String formatDate(LocalDate date) {
        return date.format(NORMAL_DATE);
    }

    /**
     * 格式化 LocalDate 返回字符串（指定格式）
     */
    private static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化 LocalDateTime 返回字符串
     * 默认格式：标准格式 yyyy-MM-dd HH:mm:ss
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_NORMAL);
    }

    /**
     * 格式化 LocalDateTime 返回字符串（指定格式）
     */
    private static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }


    /*----------------------- 日期获取 -----------------------*/

    // 获取今天
    public static String getToday() {
        return getToday(null);
    }
    public static String getToday(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now());
        }
        return formatDate(LocalDate.now(), pattern);
    }

    // 获取昨天
    public static String getYesterday() {
        return getYesterday(null);
    }
    public static String getYesterday(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now().minusDays(1));
        }
        return formatDate(LocalDate.now().minusDays(1), pattern);
    }

    // 获取明天
    public static String getTomorrow() {
        return getTomorrow(null);
    }
    public static String getTomorrow(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now().plusDays(1));
        }
        return formatDate(LocalDate.now().plusDays(1), pattern);
    }


    /*----------------------- 日期加减 -----------------------*/

    // 增加天数
    public static String plusDays(String dateStr, int days) {
        return plusDays(dateStr, days, null);
    }
    public static String plusDays(String dateStr, int days, String pattern) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(date.plusDays(days));
        }
        return formatDate(date.plusDays(days), pattern);
    }

    // 增加月份
    public static String plusMonths(String dateStr, int months) {
        return plusMonths(dateStr, months, null);
    }
    public static String plusMonths(String dateStr, int months, String pattern) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(date.plusMonths(months));
        }
        return formatDate(date.plusMonths(months), pattern);
    }

    // 增加小时数（日期时间字符串）
    public static String plusHours(String dateTimeStr, int hours) {
        return plusHours(dateTimeStr, hours, null);
    }
    public static String plusHours(String dateTimeStr, int hours, String pattern) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, resolveDateTimeFormatter(dateTimeStr));
        if (pattern == null || pattern.isEmpty()) {
            return formatDateTime(dateTime.plusHours(hours));
        }
        return formatDateTime(dateTime.plusHours(hours), pattern);
    }


    /*----------------------- 日期范围 -----------------------*/

    // 获取日期区间列表，默认标准格式
    public static List<String> getDateRange(String startDateStr, String endDateStr) {
        return getDateRange(startDateStr, endDateStr, null);
    }
    public static List<String> getDateRange(String startDateStr, String endDateStr, String pattern) {
        LocalDate start = LocalDate.parse(startDateStr, resolveDateFormatter(startDateStr));
        LocalDate end = LocalDate.parse(endDateStr, resolveDateFormatter(endDateStr));
        List<String> dates = new ArrayList<>();
        while (!start.isAfter(end)) {
            if (pattern == null || pattern.isEmpty()) {
                dates.add(formatDate(start));
            } else {
                dates.add(formatDate(start, pattern));
            }
            start = start.plusDays(1);
        }
        return dates;
    }


    /*----------------------- 日期格式转换 -----------------------*/

    /**
     * 格式化日期时间字符串，指定输入和输出格式
     * @param dateTimeStr 输入日期时间字符串
     * @param inputPattern 输入格式
     * @param outputPattern 输出格式
     * @return 转换后的字符串
     */
    public static String formatDateTime(String dateTimeStr, String inputPattern, String outputPattern) {
        LocalDateTime dt = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(inputPattern));
        return dt.format(DateTimeFormatter.ofPattern(outputPattern));
    }


    /*----------------------- 其他常用功能 -----------------------*/

    // 判断闰年
    public static boolean isLeapYear(int year) {
        return LocalDate.of(year, 1, 1).isLeapYear();
    }

    // 判断是否31天
    public static boolean isMonth31Days(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        return date.lengthOfMonth() == 31;
    }

    // 判断是否30天
    public static boolean isMonth30Days(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        return date.lengthOfMonth() == 30;
    }

    // 获取下一个指定周几，默认标准格式
    public static String getNextWeekday(String dateStr, DayOfWeek targetDay) {
        return getNextWeekday(dateStr, targetDay, null);
    }
    // 获取下一个指定周几，自定义格式
    public static String getNextWeekday(String dateStr, DayOfWeek targetDay, String pattern) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        while (date.getDayOfWeek() != targetDay) {
            date = date.plusDays(1);
        }
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(date);
        }
        return formatDate(date, pattern);
    }

    // 获取集合最大日期，默认标准格式
    public static String getMaxDate(Collection<String> dates) {
        return getMaxDate(dates, null);
    }
    // 获取集合最大日期，自定义格式
    public static String getMaxDate(Collection<String> dates, String pattern) {
        if (dates == null || dates.isEmpty()) {
            return null;
        }
        LocalDate maxDate = null;
        for (String d : dates) {
            LocalDate ld = LocalDate.parse(d, resolveDateFormatter(d));
            if (maxDate == null || ld.isAfter(maxDate)) {
                maxDate = ld;
            }
        }
        if (maxDate == null) {
            return null;
        }
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(maxDate);
        }
        return formatDate(maxDate, pattern);
    }

    // 获取集合最小日期，默认标准格式
    public static String getMinDate(Collection<String> dates) {
        return getMinDate(dates, null);
    }
    // 获取集合最小日期，自定义格式
    public static String getMinDate(Collection<String> dates, String pattern) {
        if (dates == null || dates.isEmpty()) {
            return null;
        }
        LocalDate minDate = null;
        for (String d : dates) {
            LocalDate ld = LocalDate.parse(d, resolveDateFormatter(d));
            if (minDate == null || ld.isBefore(minDate)) {
                minDate = ld;
            }
        }
        if (minDate == null) {
            return null;
        }
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(minDate);
        }
        return formatDate(minDate, pattern);
    }

}
