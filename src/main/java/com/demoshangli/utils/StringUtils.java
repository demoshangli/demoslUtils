package com.demoshangli.utils;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    // ========================= 空值处理 =========================

    /** 判断字符串是否为 null 或 "" */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /** 判断字符串是否不为空 */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /** 判断字符串是否为 null、空串或只包含空白字符 */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /** 判断字符串是否不为空白 */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /** 移除字符串中的所有空白字符（空格、制表符等） */
    public static String removeAllWhitespace(String str) {
        return (str == null) ? "" : str.replaceAll("\\s+", "");
    }

    /** 删除前后指定字符 */
    public static String trim(String str, char ch) {
        if (str == null) return "";
        return str.replaceAll("^[" + ch + "]+|[" + ch + "]+$", "");
    }

    // ========================= 字符串格式转换 =========================

    /** 首字母大写 */
    public static String capitalizeFirst(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /** 首字母小写 */
    public static String uncapitalizeFirst(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /** 驼峰转下划线（如: userName -> user_name） */
    public static String camelToUnderscore(String str) {
        if (str == null) return "";
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /** 下划线转驼峰（如: user_name -> userName） */
    public static String underscoreToCamel(String str) {
        if (str == null) return "";
        String[] parts = str.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    // ========================= 补齐与截取 =========================

    /** 左侧补全字符到指定长度 */
    public static String leftPad(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - str.length()) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /** 右侧补全字符到指定长度 */
    public static String rightPad(String str, int length, char padChar) {
        if (str == null) str = "";
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    // ========================= 编码转换 =========================

    /** 将字符串从 ISO8859-1 编码转换为 UTF-8 编码 */
    public static String isoToUtf8(String str) {
        if (str == null) return "";
        try {
            return new String(str.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /** 将字符串从 UTF-8 编码转换为 ISO8859-1 编码 */
    public static String utf8ToIso(String str) {
        if (str == null) return "";
        try {
            return new String(str.getBytes("UTF-8"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    // ========================= HTML 转义 =========================

    /** 将 HTML 标签进行转义（防止 XSS） */
    public static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** 将 HTML 标签反转义 */
    public static String unescapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }
}
