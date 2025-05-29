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
 * 依赖：Bouncy Castle 1.70+
 *
 * 安全提示：
 * 1. MD5已被证明不安全（存在碰撞漏洞），仅适用于兼容旧系统
 * 2. DES密钥过短（56位有效密钥），易被暴力破解，建议使用AES替代
 * 3. 3DES已逐渐被淘汰（NIST建议2030年后停用），推荐使用AES-256
 * 4. RC4存在严重安全漏洞（Fluhrer-Mantin-Shamir攻击），应避免使用
 * 5. RC5密钥长度可变但专利限制，且少于12轮的实现存在弱点
 * 6. IDEA专利已过期但使用不广泛，需谨慎评估实现安全性
 * 7. SM3作为国密算法安全性较高，但实现依赖BouncyCastle库
 * 8. AES-ECB模式存在安全性问题，推荐改用CBC/GCM等带IV的模式
 * 9. RSA加密有长度限制，长文本应分段处理或改用混合加密
 * 10. 实际生产环境应使用密钥管理系统（KMS/HSM），避免硬编码密钥
 * 11. AES-256是目前推荐的分组加密标准（使用GCM模式可提供认证加密）
 * 12. 使用固定IV或静态密钥会显著降低系统安全性
 * 13. RC4已被主流标准废弃（RFC 7465），仅用于遗留系统兼容
 */
public class CryptoUtils {

    static {
        // 注册BouncyCastle安全提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================== 哈希算法 =====================
    
    // SM3 哈希
    public static String sm3(String input) {
        byte[] hash = sm3Hash(input.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(hash);
    }

    // SM3 带密钥的 HMAC 计算
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

    // SM3 带密钥的 HMAC 计算实现
    private static byte[] hmacSm3(byte[] key, byte[] input) {
        HMac hmac = new HMac(new SM3Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(input, 0, input.length);
        byte[] result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        return result;
    }

    // HMAC-SM3 哈希计算（带盐值增强）
    public static String hmacSm3WithSalt(String keyText, String plainText, byte[] salt) {
        byte[] combined = ByteUtils.concat(
                plainText.getBytes(StandardCharsets.UTF_8),
                salt
        );
        return hmacSm3(keyText, new String(combined, StandardCharsets.UTF_8));
    }

    // ByteUtils 用于字节数组操作
    private static class ByteUtils {
        static String toHexString(byte[] bytes) {
            return Hex.toHexString(bytes);
        }

        static byte[] concat(byte[]... arrays) {
            // 实现字节数组合并逻辑
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

    // MD5 哈希
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    // SHA-256 哈希
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
    
    // AES-128 ECB模式（不推荐）
    public static String aesEncrypt(String plaintext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String aesDecrypt(String ciphertext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // AES-256 GCM模式（推荐）
    public static String aes256Encrypt(String plaintext, String key) throws Exception {
        return aesGcmEncrypt(plaintext, key);
    }
    
    public static String aes256Decrypt(String ciphertext, String key) throws Exception {
        return aesGcmDecrypt(ciphertext, key);
    }
    
    // AES-GCM 加密实现
    private static String aesGcmEncrypt(String plaintext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes (256 bits)");
        }
        
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        
        // 生成随机IV（12字节）
        byte[] iv = new byte[12];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
        
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // 组合IV和密文
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    // AES-GCM 解密实现
    private static String aesGcmDecrypt(String ciphertext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes (256 bits)");
        }
        
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        byte[] combined = Base64.getDecoder().decode(ciphertext);
        
        // 分离IV和密文
        byte[] iv = Arrays.copyOfRange(combined, 0, 12);
        byte[] encrypted = Arrays.copyOfRange(combined, 12, combined.length);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // ===================== DES 加解密 =====================
    public static String desEncrypt(String plaintext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String desDecrypt(String ciphertext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // ===================== 3DES 加解密 =====================
    public static String tripleDesEncrypt(String plaintext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 24) {
            throw new IllegalArgumentException("3DES key must be 24 bytes (192 bits)");
        }
        
        SecretKey secretKey = new SecretKeySpec(keyBytes, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String tripleDesDecrypt(String ciphertext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 24) {
            throw new IllegalArgumentException("3DES key must be 24 bytes (192 bits)");
        }
        
        SecretKey secretKey = new SecretKeySpec(keyBytes, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // ===================== RC5 加解密 =====================
    public static String rc5Encrypt(String plaintext, String key, int rounds) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        
        RC564Engine engine = new RC564Engine();
        engine.init(true, new RC5Parameters(keyBytes, rounds));
        
        // 填充输入数据使其长度为块大小的倍数
        int blockSize = engine.getBlockSize();
        int paddedLength = ((inputBytes.length + blockSize - 1) / blockSize) * blockSize;
        byte[] paddedInput = new byte[paddedLength];
        System.arraycopy(inputBytes, 0, paddedInput, 0, inputBytes.length);
        
        byte[] output = new byte[paddedInput.length];
        for (int i = 0; i < paddedInput.length; i += blockSize) {
            engine.processBlock(paddedInput, i, output, i);
        }
        
        return Base64.getEncoder().encodeToString(output);
    }

    public static String rc5Decrypt(String ciphertext, String key, int rounds) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
        
        RC564Engine engine = new RC564Engine();
        engine.init(false, new RC5Parameters(keyBytes, rounds));
        
        byte[] output = new byte[encryptedBytes.length];
        int blockSize = engine.getBlockSize();
        for (int i = 0; i < encryptedBytes.length; i += blockSize) {
            engine.processBlock(encryptedBytes, i, output, i);
        }
        
        // 移除填充
        int padding = 0;
        for (int i = output.length - 1; i >= 0; i--) {
            if (output[i] != 0) break;
            padding++;
        }
        byte[] result = new byte[output.length - padding];
        System.arraycopy(output, 0, result, 0, result.length);
        
        return new String(result, StandardCharsets.UTF_8);
    }

    // ===================== IDEA 加解密 =====================
    public static String ideaEncrypt(String plaintext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        
        IDEAEngine engine = new IDEAEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine));
        cipher.init(true, new KeyParameter(keyBytes));
        
        byte[] output = new byte[cipher.getOutputSize(inputBytes.length)];
        int outputLength = cipher.processBytes(inputBytes, 0, inputBytes.length, output, 0);
        outputLength += cipher.doFinal(output, outputLength);
        
        return Base64.getEncoder().encodeToString(Arrays.copyOf(output, outputLength));
    }

    public static String ideaDecrypt(String ciphertext, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
        
        IDEAEngine engine = new IDEAEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine));
        cipher.init(false, new KeyParameter(keyBytes));
        
        byte[] output = new byte[cipher.getOutputSize(encryptedBytes.length)];
        int outputLength = cipher.processBytes(encryptedBytes, 0, encryptedBytes.length, output, 0);
        outputLength += cipher.doFinal(output, outputLength);
        
        return new String(Arrays.copyOf(output, outputLength), StandardCharsets.UTF_8);
    }

    // ===================== RC4 加解密 =====================
    public static String rc4Encrypt(String plaintext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "RC4");
        Cipher cipher = Cipher.getInstance("RC4");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String rc4Decrypt(String ciphertext, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "RC4");
        Cipher cipher = Cipher.getInstance("RC4");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // ===================== RSA 加解密 =====================
    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public static String rsaEncrypt(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String rsaDecrypt(String ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // ===================== 辅助方法 =====================
    public static PublicKey getPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

}