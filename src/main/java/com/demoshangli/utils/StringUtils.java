package com.demoshangli.utils;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    // ========================= 空值处理 =========================

    /**
     * 判断字符串是否为 null 或 ""
     *
     * @param str 需要判断的字符串
     * @return 如果字符串为 null 或 空字符串，返回 true；否则返回 false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 需要判断的字符串
     * @return 如果字符串不为空，返回 true；否则返回 false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为 null、空串或只包含空白字符（如空格、制表符等）
     *
     * @param str 需要判断的字符串
     * @return 如果字符串为 null 或 空白字符，返回 true；否则返回 false
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 需要判断的字符串
     * @return 如果字符串不为空白，返回 true；否则返回 false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 移除字符串中的所有空白字符（包括空格、制表符等）
     *
     * @param str 需要处理的字符串
     * @return 移除所有空白字符后的字符串，如果原字符串为 null，返回空字符串
     */
    public static String removeAllWhitespace(String str) {
        return (str == null) ? "" : str.replaceAll("\\s+", "");
    }

    /**
     * 删除字符串前后指定字符
     *
     * @param str 需要处理的字符串
     * @param ch 需要删除的字符
     * @return 删除前后指定字符后的字符串，如果原字符串为 null，返回空字符串
     */
    public static String trim(String str, char ch) {
        if (str == null) return "";
        return str.replaceAll("^[" + ch + "]+|[" + ch + "]+$", "");
    }

    // ========================= 字符串格式转换 =========================

    /**
     * 将字符串的首字母转换为大写
     *
     * @param str 需要处理的字符串
     * @return 首字母大写后的字符串，如果字符串为空或 null，直接返回原字符串
     */
    public static String capitalizeFirst(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将字符串的首字母转换为小写
     *
     * @param str 需要处理的字符串
     * @return 首字母小写后的字符串，如果字符串为空或 null，直接返回原字符串
     */
    public static String uncapitalizeFirst(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 将驼峰命名法（CamelCase）字符串转换为下划线命名法（snake_case）
     *
     * @param str 需要转换的字符串（驼峰命名法）
     * @return 转换后的下划线命名法字符串
     */
    public static String camelToUnderscore(String str) {
        if (str == null) return "";
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * 将下划线命名法（snake_case）字符串转换为驼峰命名法（CamelCase）
     *
     * @param str 需要转换的字符串（下划线命名法）
     * @return 转换后的驼峰命名法字符串
     */
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

    /**
     * 在字符串的左侧补充指定字符，直到字符串达到指定长度
     *
     * @param str 需要补充的字符串
     * @param length 目标长度
     * @param padChar 用于补充的字符
     * @return 左侧补充字符后的字符串，如果原字符串已经大于或等于目标长度，返回原字符串
     */
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

    /**
     * 在字符串的右侧补充指定字符，直到字符串达到指定长度
     *
     * @param str 需要补充的字符串
     * @param length 目标长度
     * @param padChar 用于补充的字符
     * @return 右侧补充字符后的字符串，如果原字符串已经大于或等于目标长度，返回原字符串
     */
    public static String rightPad(String str, int length, char padChar) {
        if (str == null) str = "";
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    // ========================= 编码转换 =========================

    /**
     * 将字符串从 ISO8859-1 编码转换为 UTF-8 编码
     *
     * @param str 需要转换编码的字符串
     * @return 转换后的 UTF-8 编码字符串，如果转换失败，返回原字符串
     */
    public static String isoToUtf8(String str) {
        if (str == null) return "";
        try {
            return new String(str.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * 将字符串从 UTF-8 编码转换为 ISO8859-1 编码
     *
     * @param str 需要转换编码的字符串
     * @return 转换后的 ISO8859-1 编码字符串，如果转换失败，返回原字符串
     */
    public static String utf8ToIso(String str) {
        if (str == null) return "";
        try {
            return new String(str.getBytes("UTF-8"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    // ========================= HTML 转义 =========================

    /**
     * 将 HTML 标签进行转义，防止 XSS 攻击
     *
     * @param str 需要转义的字符串
     * @return 转义后的字符串，所有 HTML 特殊字符将被替换为实体字符
     */
    public static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 将 HTML 标签反转义，将实体字符恢复为普通字符
     *
     * @param str 需要反转义的字符串
     * @return 反转义后的字符串，所有 HTML 实体字符将恢复为原始字符
     */
    public static String unescapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }
}
