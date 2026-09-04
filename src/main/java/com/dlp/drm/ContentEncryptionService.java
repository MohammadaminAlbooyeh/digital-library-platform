package com.dlp.drm;

import com.dlp.kms.KmsDataKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ContentEncryptionService {

    private static final int IV_LENGTH = 16;

    private final SecretKeySpec keySpec;
    private final KmsDataKeyService kmsService;
    private final SecureRandom secureRandom;

    public ContentEncryptionService(@Value("${app.drm.encryption-key}") String encryptionKey,
                                    KmsDataKeyService kmsService) {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[16];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, 16));
        this.keySpec = new SecretKeySpec(normalized, "AES");
        this.kmsService = kmsService;
        this.secureRandom = new SecureRandom();
    }

    public byte[] encrypt(byte[] plaintext) throws Exception {
        return encrypt(plaintext, generateContentKey());
    }

    public byte[] encrypt(byte[] plaintext, String contentId) throws Exception {
        SecretKey key = kmsService.deriveContentKey(contentId);
        return encrypt(plaintext, key);
    }

    private byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext);
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }

    public byte[] decrypt(byte[] ciphertext) throws Exception {
        return decrypt(ciphertext, generateContentKey());
    }

    public byte[] decrypt(byte[] ciphertext, String contentId) throws Exception {
        SecretKey key = kmsService.deriveContentKey(contentId);
        return decrypt(ciphertext, key);
    }

    private byte[] decrypt(byte[] data, SecretKey key) throws Exception {
        if (data.length < IV_LENGTH) {
            throw new IllegalArgumentException("Ciphertext too short");
        }
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[data.length - IV_LENGTH];
        System.arraycopy(data, 0, iv, 0, IV_LENGTH);
        System.arraycopy(data, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(encrypted);
    }

    private SecretKey generateContentKey() {
        return keySpec;
    }

    public String encryptToBase64(String plaintext) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptFromBase64(String base64) throws Exception {
        return new String(decrypt(Base64.getDecoder().decode(base64)), StandardCharsets.UTF_8);
    }
}
