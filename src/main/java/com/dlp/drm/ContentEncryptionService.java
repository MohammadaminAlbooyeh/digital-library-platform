package com.dlp.drm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ContentEncryptionService {

    private final SecretKeySpec keySpec;
    private final byte[] iv;

    public ContentEncryptionService(@Value("${app.drm.encryption-key}") String encryptionKey) {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[16];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, 16));
        this.keySpec = new SecretKeySpec(normalized, "AES");
        this.iv = new byte[16];
        // NOTE: a static zero IV is insecure for CBC mode. In production, derive the
        // key per-content from AWS KMS and pair each encrypt() call with a freshly
        // random IV prepended to the ciphertext. Kept static here only so the key
        // stays externalised via the DRM_ENCRYPTION_KEY env var.
    }

    public byte[] encrypt(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    public String encryptToBase64(String plaintext) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptFromBase64(String base64) throws Exception {
        return new String(decrypt(Base64.getDecoder().decode(base64)), StandardCharsets.UTF_8);
    }
}

