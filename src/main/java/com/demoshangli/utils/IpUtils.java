package com.demoshangli.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Ip工具类 - 提供简易IP操作。
 */
public class IpUtils {

    // 私有构造，防止实例化
    private IpUtils() {}

    // IPv4正则（简化版，支持0-255）
    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)" +
                    "(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}$";

    // 用于匹配IPv4地址的正则表达式模式
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    // 简单IPv6正则（基本校验）
    private static final String IPV6_REGEX =
            "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}";

    // 用于匹配IPv6地址的正则表达式模式
    private static final Pattern IPV6_PATTERN = Pattern.compile(IPV6_REGEX);

    /**
     * 获取本机内网IP地址（优先返回非回环地址）
     * 本方法会遍历本地网络接口，返回非回环地址的IPv4地址。
     * @return 本机内网IPv4地址，如果没有可用的内网地址则返回“127.0.0.1”。
     */
    public static String getLocalIp() {
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while(networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 如果地址不是回环地址并且是IPv4地址，则返回该地址
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // 如果发生异常，则跳过并尝试下一种方式
        }
        // 如果无法获取内网IP，则回退返回“127.0.0.1”
        return "127.0.0.1";
    }

    /**
     * 获取外网IP（通过访问公网接口）
     * 通过调用一个外部服务（如ipify）来获取当前机器的公网IP。
     * @return 外网IP地址，如果获取失败则返回空字符串。
     */
    public static String getPublicIp() {
        String ipService = "https://api.ipify.org"; // 简单接口，返回纯文本IP
        BufferedReader in = null;
        try {
            // 请求外部服务，获取公网IP
            URL url = new URL(ipService);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(3000); // 设置连接超时为3秒
            conn.setReadTimeout(3000); // 设置读取超时为3秒
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return in.readLine(); // 返回响应的IP地址
        } catch (Exception e) {
            return ""; // 获取失败时返回空字符串
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 判断字符串是否是有效的IPv4地址
     * 通过正则表达式验证给定的字符串是否符合IPv4地址的格式。
     * @param ip 待验证的IP地址字符串
     * @return 如果是有效的IPv4地址，返回true；否则返回false。
     */
    public static boolean isIPv4(String ip) {
        if (ip == null) return false;
        return IPV4_PATTERN.matcher(ip).matches(); // 使用IPv4正则表达式验证
    }

    /**
     * 判断字符串是否是有效的IPv6地址（简单匹配）
     * 通过正则表达式验证给定的字符串是否符合IPv6地址的格式。
     * @param ip 待验证的IP地址字符串
     * @return 如果是有效的IPv6地址，返回true；否则返回false。
     */
    public static boolean isIPv6(String ip) {
        if (ip == null) return false;
        return IPV6_PATTERN.matcher(ip).matches(); // 使用IPv6正则表达式验证
    }

    /**
     * 判断是否是内网IP（私有地址段）
     * 判断给定的IPv4地址是否属于内网IP地址（即私有IP地址段）。
     * @param ip 待验证的IP地址字符串
     * @return 如果是内网IP地址，返回true；否则返回false。
     */
    public static boolean isPrivateIp(String ip) {
        if (!isIPv4(ip)) return false; // 只处理IPv4地址
        long ipNum = ipToLong(ip); // 将IP地址转为long类型进行比较
        // 10.0.0.0 - 10.255.255.255
        if (ipNum >= ipToLong("10.0.0.0") && ipNum <= ipToLong("10.255.255.255")) {
            return true;
        }
        // 172.16.0.0 - 172.31.255.255
        if (ipNum >= ipToLong("172.16.0.0") && ipNum <= ipToLong("172.31.255.255")) {
            return true;
        }
        // 192.168.0.0 - 192.168.255.255
        if (ipNum >= ipToLong("192.168.0.0") && ipNum <= ipToLong("192.168.255.255")) {
            return true;
        }
        return false; // 不是内网IP
    }

    /**
     * 将IPv4地址转换为long数字
     * 将一个IPv4地址转换为一个长整型数字，以便进行数值比较。
     * @param ip 待转换的IPv4地址字符串
     * @return 转换后的长整型数字表示的IP地址
     * @throws IllegalArgumentException 如果传入的字符串不是有效的IPv4地址，则抛出异常
     */
    public static long ipToLong(String ip) {
        if (!isIPv4(ip)) throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        String[] parts = ip.split("\\."); // 将IP地址按"."分割成四个部分
        long result = 0;
        for (String part : parts) {
            result = (result << 8) + Integer.parseInt(part); // 将每个部分转换为数字并合并
        }
        return result;
    }

    /**
     * 将long数字转换回IPv4地址字符串
     * 将一个长整型数字转换回对应的IPv4地址字符串。
     * @param ipLong 长整型数字表示的IPv4地址
     * @return 转换后的IPv4地址字符串
     */
    public static String longToIp(long ipLong) {
        return String.format("%d.%d.%d.%d",
                (ipLong >> 24) & 0xFF,
                (ipLong >> 16) & 0xFF,
                (ipLong >> 8) & 0xFF,
                ipLong & 0xFF); // 通过位运算获取各个字节
    }

    /**
     * 判断IP是否在指定的IP段范围内（含首尾）
     * 判断一个IP地址是否在给定的IP范围内，包含起始和结束IP。
     * @param ip 待判断的IP地址
     * @param startIp IP段的起始地址
     * @param endIp IP段的结束地址
     * @return 如果IP在指定范围内，返回true；否则返回false。
     */
    public static boolean isIpInRange(String ip, String startIp, String endIp) {
        if (!isIPv4(ip) || !isIPv4(startIp) || !isIPv4(endIp)) {
            return false; // 如果IP或范围不合法，返回false
        }
        long ipNum = ipToLong(ip); // 将IP转换为long类型
        return ipNum >= ipToLong(startIp) && ipNum <= ipToLong(endIp); // 判断IP是否在范围内
    }
}
