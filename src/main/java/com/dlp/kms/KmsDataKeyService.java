package com.dlp.kms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Component
public class KmsDataKeyService {

    private static final int KEY_LENGTH = 16;

    private final SecretKeySpec fallbackKey;
    private final String kmsKeyId;
    private final KmsClient kmsClient;
    private final boolean kmsEnabled;

    public KmsDataKeyService(@Value("${app.drm.encryption-key}") String encryptionKey,
                             @Value("${app.drm.kms-key-id:}") String kmsKeyId,
                             @Value("${app.aws.s3.region:us-east-1}") String region) {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[KEY_LENGTH];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, KEY_LENGTH));
        this.fallbackKey = new SecretKeySpec(normalized, "AES");
        this.kmsKeyId = kmsKeyId;
        this.kmsEnabled = kmsKeyId != null && !kmsKeyId.isBlank();
        this.kmsClient = this.kmsEnabled
                ? KmsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build()
                : null;
    }

    public SecretKeySpec deriveContentKey(String contentId) {
        if (kmsEnabled) {
            return deriveKeyViaKms(contentId);
        }
        return deriveKeyLocally(contentId);
    }

    private SecretKeySpec deriveKeyViaKms(String contentId) {
        try {
            GenerateDataKeyRequest request = GenerateDataKeyRequest.builder()
                    .keyId(kmsKeyId)
                    .keySpec(DataKeySpec.AES_128)
                    .encryptionContext(Map.of("contentId", contentId))
                    .build();
            GenerateDataKeyResponse response = kmsClient.generateDataKey(request);
            byte[] plaintextKey = response.plaintext().asByteArray();
            if (plaintextKey.length < KEY_LENGTH) {
                byte[] padded = new byte[KEY_LENGTH];
                System.arraycopy(plaintextKey, 0, padded, 0, plaintextKey.length);
                return new SecretKeySpec(padded, "AES");
            }
            return new SecretKeySpec(plaintextKey, 0, KEY_LENGTH, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive KMS data key for content " + contentId, e);
        }
    }

    private SecretKeySpec deriveKeyLocally(String contentId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fallbackKey.getEncoded());
            digest.update(contentId.getBytes(StandardCharsets.UTF_8));
            byte[] derived = digest.digest();
            byte[] key = new byte[KEY_LENGTH];
            System.arraycopy(derived, 0, key, 0, KEY_LENGTH);
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive local content key", e);
        }
    }

    public boolean isKmsEnabled() {
        return kmsEnabled;
    }
}
