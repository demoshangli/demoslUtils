package com.demoshangli.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期工具类，提供日期和时间的格式化、加减、比较等常见操作。
 * 支持日期字符串自动格式识别（yyyyMMdd 和 yyyy-MM-dd），
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
     * 根据传入的日期字符串自动识别日期格式（不含时间）。
     * 支持的格式包括：yyyyMMdd 和 yyyy-MM-dd 两种格式。
     *
     * @param dateStr 输入的日期字符串
     * @return 返回识别到的日期格式化器
     * @throws IllegalArgumentException 如果输入的日期字符串不符合支持的格式
     */
    private static DateTimeFormatter resolveDateFormatter(String dateStr) {
        if (dateStr == null) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        // 判断是否包含 "-" 符号来区分标准和紧凑格式
        if (dateStr.contains("-")) {
            if (dateStr.length() == 10) {
                return NORMAL_DATE;
            }
            throw new IllegalArgumentException("不支持的日期格式：" + dateStr);
        } else {
            if (dateStr.length() == 8) {
                return COMPACT_DATE;
            }
            throw new IllegalArgumentException("不支持的日期格式：" + dateStr);
        }
    }

    /**
     * 根据传入的日期时间字符串自动识别日期时间格式。
     * 支持的格式包括：yyyyMMddHHmmss 和 yyyy-MM-dd HH:mm:ss 两种格式。
     *
     * @param dateTimeStr 输入的日期时间字符串
     * @return 返回识别到的日期时间格式化器
     * @throws IllegalArgumentException 如果输入的日期时间字符串不符合支持的格式
     */
    private static DateTimeFormatter resolveDateTimeFormatter(String dateTimeStr) {
        if (dateTimeStr == null) {
            throw new IllegalArgumentException("日期时间字符串不能为空");
        }
        // 判断是否包含 "-" 符号来区分标准和紧凑格式
        if (dateTimeStr.contains("-")) {
            if (dateTimeStr.length() == 19) {
                return DATETIME_NORMAL;
            }
            throw new IllegalArgumentException("不支持的日期时间格式：" + dateTimeStr);
        } else {
            if (dateTimeStr.length() == 14) {
                return DATETIME_COMPACT;
            }
            throw new IllegalArgumentException("不支持的日期时间格式：" + dateTimeStr);
        }
    }

    /**
     * 格式化 LocalDate 返回字符串，默认格式为 yyyy-MM-dd。
     *
     * @param date 要格式化的 LocalDate 对象
     * @return 格式化后的日期字符串
     */
    private static String formatDate(LocalDate date) {
        return date.format(NORMAL_DATE);
    }

    /**
     * 格式化 LocalDate 返回字符串，指定格式。
     *
     * @param date 要格式化的 LocalDate 对象
     * @param pattern 格式化模式（如：yyyyMMdd）
     * @return 格式化后的日期字符串
     */
    private static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化 LocalDateTime 返回字符串，默认格式为 yyyy-MM-dd HH:mm:ss。
     *
     * @param dateTime 要格式化的 LocalDateTime 对象
     * @return 格式化后的日期时间字符串
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_NORMAL);
    }

    /**
     * 格式化 LocalDateTime 返回字符串，指定格式。
     *
     * @param dateTime 要格式化的 LocalDateTime 对象
     * @param pattern 格式化模式（如：yyyyMMddHHmmss）
     * @return 格式化后的日期时间字符串
     */
    private static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }


    /*----------------------- 日期获取 -----------------------*/

    /**
     * 获取今天的日期，使用标准格式 yyyy-MM-dd。
     *
     * @return 今天的日期字符串
     */
    public static String getToday() {
        return getToday(null);
    }

    /**
     * 获取今天的日期，支持自定义返回格式。
     *
     * @param pattern 日期格式，若为 null 则返回标准格式
     * @return 今天的日期字符串
     */
    public static String getToday(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now());
        }
        return formatDate(LocalDate.now(), pattern);
    }

    /**
     * 获取昨天的日期，使用标准格式 yyyy-MM-dd。
     *
     * @return 昨天的日期字符串
     */
    public static String getYesterday() {
        return getYesterday(null);
    }

    /**
     * 获取昨天的日期，支持自定义返回格式。
     *
     * @param pattern 日期格式，若为 null 则返回标准格式
     * @return 昨天的日期字符串
     */
    public static String getYesterday(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now().minusDays(1));
        }
        return formatDate(LocalDate.now().minusDays(1), pattern);
    }

    /**
     * 获取明天的日期，使用标准格式 yyyy-MM-dd。
     *
     * @return 明天的日期字符串
     */
    public static String getTomorrow() {
        return getTomorrow(null);
    }

    /**
     * 获取明天的日期，支持自定义返回格式。
     *
     * @param pattern 日期格式，若为 null 则返回标准格式
     * @return 明天的日期字符串
     */
    public static String getTomorrow(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(LocalDate.now().plusDays(1));
        }
        return formatDate(LocalDate.now().plusDays(1), pattern);
    }


    /*----------------------- 日期加减 -----------------------*/

    /**
     * 增加指定天数到日期字符串中，返回结果为指定格式。
     *
     * @param dateStr 输入的日期字符串
     * @param days 要增加的天数
     * @return 增加天数后的日期字符串
     */
    public static String plusDays(String dateStr, int days) {
        return plusDays(dateStr, days, null);
    }

    /**
     * 增加指定天数到日期字符串中，返回结果为指定格式。
     *
     * @param dateStr 输入的日期字符串
     * @param days 要增加的天数
     * @param pattern 返回结果的格式，若为 null 则使用标准格式
     * @return 增加天数后的日期字符串
     */
    public static String plusDays(String dateStr, int days, String pattern) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(date.plusDays(days));
        }
        return formatDate(date.plusDays(days), pattern);
    }

    /**
     * 增加指定月份到日期字符串中，返回结果为指定格式。
     *
     * @param dateStr 输入的日期字符串
     * @param months 要增加的月份
     * @return 增加月份后的日期字符串
     */
    public static String plusMonths(String dateStr, int months) {
        return plusMonths(dateStr, months, null);
    }

    /**
     * 增加指定月份到日期字符串中，返回结果为指定格式。
     *
     * @param dateStr 输入的日期字符串
     * @param months 要增加的月份
     * @param pattern 返回结果的格式，若为 null 则使用标准格式
     * @return 增加月份后的日期字符串
     */
    public static String plusMonths(String dateStr, int months, String pattern) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        if (pattern == null || pattern.isEmpty()) {
            return formatDate(date.plusMonths(months));
        }
        return formatDate(date.plusMonths(months), pattern);
    }

    /*----------------------- 判断日期 -----------------------*/

    /**
     * 判断给定年份是否为闰年。
     *
     * @param year 待判断的年份
     * @return 如果是闰年返回 true，否者返回 false
     */
    public static boolean isLeapYear(int year) {
        return LocalDate.of(year, 2, 29).getMonthValue() == 2;
    }

    /**
     * 判断一个日期字符串是否为有效日期。
     *
     * @param dateStr 日期字符串
     * @return 如果是有效日期返回 true，否则返回 false
     */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取给定日期所在月份的天数。
     *
     * @param dateStr 日期字符串
     * @return 该日期所在月份的天数
     */
    public static int getMonthDays(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        return date.lengthOfMonth();
    }

    /*----------------------- 获取星期几 -----------------------*/

    /**
     * 获取给定日期的星期几。
     *
     * @param dateStr 输入的日期字符串
     * @return 对应的星期几名称
     */
    public static String getDayOfWeek(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        return date.getDayOfWeek().toString();
    }

    /**
     * 获取指定日期的下一个指定星期几的日期。
     *
     * @param dateStr 输入的日期字符串
     * @param dayOfWeek 要获取的目标星期几（如 MONDAY、TUESDAY）
     * @return 下一个指定星期几的日期字符串
     */
    public static String getNextDayOfWeek(String dateStr, DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.parse(dateStr, resolveDateFormatter(dateStr));
        LocalDate nextDate = date.with(java.time.temporal.TemporalAdjusters.next(dayOfWeek));
        return formatDate(nextDate);
    }
}
