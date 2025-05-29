package com.demoshangli.utils;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RC5Parameters;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.engines.RC564Engine;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * 综合加密工具类（包含分组加密和流加密算法）
 * 依赖：Bouncy Castle 1.70+ （需要在项目中加入Bouncy Castle库）
 *
 * 安全提示：
 * 1. MD5已被证明不安全（存在碰撞漏洞），仅适用于兼容旧系统。
 * 2. DES密钥过短（56位有效密钥），易被暴力破解，建议使用AES替代。
 * 3. 3DES已逐渐被淘汰（NIST建议2030年后停用），推荐使用AES-256。
 * 4. RC4存在严重安全漏洞（Fluhrer-Mantin-Shamir攻击），应避免使用。
 * 5. RC5密钥长度可变但专利限制，且少于12轮的实现存在弱点。
 * 6. IDEA专利已过期但使用不广泛，需谨慎评估实现安全性。
 * 7. SM3作为国密算法安全性较高，但实现依赖BouncyCastle库。
 * 8. AES-ECB模式存在安全性问题，推荐改用CBC/GCM等带IV的模式。
 * 9. RSA加密有长度限制，长文本应分段处理或改用混合加密。
 * 10. 实际生产环境应使用密钥管理系统（KMS/HSM），避免硬编码密钥。
 * 11. AES-256是目前推荐的分组加密标准（使用GCM模式可提供认证加密）。
 * 12. 使用固定IV或静态密钥会显著降低系统安全性。
 * 13. RC4已被主流标准废弃（RFC 7465），仅用于遗留系统兼容。
 * 14. AES-GCM模式不仅提供加密还提供数据完整性校验，推荐用于生产环境。
 */
public class CryptoUtils {

    static {
        // 注册BouncyCastle作为安全提供者，用于支持各种加密算法
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================== 哈希算法 =====================

    /**
     * 使用SM3算法进行哈希计算
     * @param input 输入数据
     * @return 返回SM3算法的哈希值
     */
    public static String sm3(String input) {
        byte[] hash = sm3Hash(input.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(hash);
    }

    /**
     * 使用SM3算法计算带密钥的HMAC
     * @param key 密钥
     * @param input 输入数据
     * @return 返回带密钥的SM3 HMAC值
     */
    public static String hmacSm3(String key, String input) {
        byte[] mac = hmacSm3(
                key.getBytes(StandardCharsets.UTF_8),
                input.getBytes(StandardCharsets.UTF_8)
        );
        return Hex.toHexString(mac);
    }

    // SM3哈希实现
    private static byte[] sm3Hash(byte[] input) {
        SM3Digest digest = new SM3Digest();
        digest.update(input, 0, input.length);
        byte[] output = new byte[digest.getDigestSize()];
        digest.doFinal(output, 0);
        return output;
    }

    // SM3带密钥的HMAC实现
    private static byte[] hmacSm3(byte[] key, byte[] input) {
        HMac hmac = new HMac(new SM3Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(input, 0, input.length);
        byte[] result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        return result;
    }

    /**
     * 使用SM3计算带盐值增强的HMAC
     * @param keyText 密钥
     * @param plainText 明文数据
     * @param salt 盐值
     * @return 返回带盐值增强的HMAC值
     */
    public static String hmacSm3WithSalt(String keyText, String plainText, byte[] salt) {
        byte[] combined = ByteUtils.concat(
                plainText.getBytes(StandardCharsets.UTF_8),
                salt
        );
        return hmacSm3(keyText, new String(combined, StandardCharsets.UTF_8));
    }

    // ByteUtils 用于字节数组操作
    private static class ByteUtils {
        /**
         * 将字节数组转为16进制字符串
         * @param bytes 字节数组
         * @return 16进制字符串
         */
        static String toHexString(byte[] bytes) {
            return Hex.toHexString(bytes);
        }

        /**
         * 合并多个字节数组
         * @param arrays 字节数组列表
         * @return 合并后的字节数组
         */
        static byte[] concat(byte[]... arrays) {
            int totalLength = 0;
            for (byte[] array : arrays) {
                totalLength += array.length;
            }
            byte[] result = new byte[totalLength];
            int offset = 0;
            for (byte[] array : arrays) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
            return result;
        }
    }

    /**
     * 使用MD5算法计算哈希值（不安全，推荐仅用于兼容老系统）
     * @param input 输入数据
     * @return MD5哈希值
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * 使用SHA-256算法计算哈希值
     * @param input 输入数据
     * @return SHA-256哈希值
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ===================== AES 加解密 =====================

    /**
     * 使用AES-128 ECB模式加密（不推荐，建议使用GCM或CBC）
     * @param plaintext 明文
     * @param key 密钥
     * @return 加密后的密文
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String aesEncrypt(String plaintext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 使用AES-128 ECB模式解密
     * @param ciphertext 密文
     * @param key 密钥
     * @return 解密后的明文
     * @throws Exception 解密过程中可能抛出的异常
     */
    public static String aesDecrypt(String ciphertext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // ===================== AES-GCM 加解密 =====================

    /**
     * 使用AES-256 GCM模式加密（推荐，提供认证加密）
     * @param plaintext 明文
     * @param key 密钥（必须是32字节）
     * @return 加密后的密文
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String aes256Encrypt(String plaintext, String key) throws Exception {
        return aesGcmEncrypt(plaintext, key);
    }

    /**
     * 使用AES-256 GCM模式解密
     * @param ciphertext 密文
     * @param key 密钥（必须是32字节）
     * @return 解密后的明文
     * @throws Exception 解密过程中可能抛出的异常
     */
    public static String aes256Decrypt(String ciphertext, String key) throws Exception {
        return aesGcmDecrypt(ciphertext, key);
    }

    // 通用AES-GCM加密实现
    private static String aesGcmEncrypt(String plaintext, String key) throws Exception {
        byte[] iv = new byte[12]; // 12字节IV
        new SecureRandom().nextBytes(iv); // 随机生成IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128位标签长度
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ByteUtils.concat(iv, ciphertext)); // 将IV和密文一起返回
    }

    // 通用AES-GCM解密实现
    private static String aesGcmDecrypt(String ciphertext, String key) throws Exception {
        byte[] cipherData = Base64.getDecoder().decode(ciphertext);
        byte[] iv = Arrays.copyOfRange(cipherData, 0, 12); // 提取IV
        byte[] encData = Arrays.copyOfRange(cipherData, 12, cipherData.length); // 提取密文
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] decrypted = cipher.doFinal(encData);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}