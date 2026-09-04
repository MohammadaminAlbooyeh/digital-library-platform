package com.dlp.kms;

import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KmsDataKeyServiceTest {

    private KmsDataKeyService service() {
        return new KmsDataKeyService(
                "0123456789abcdef0123456789abcdef",
                "",
                "us-east-1"
        );
    }

    @Test
    void localModeDerivesDeterministicKey() {
        KmsDataKeyService kms = service();
        SecretKeySpec key1 = kms.deriveContentKey("content-1");
        SecretKeySpec key2 = kms.deriveContentKey("content-1");

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(),
                "Same content ID should produce the same key in local mode");
    }

    @Test
    void differentContentIdsProduceDifferentKeys() {
        KmsDataKeyService kms = service();
        SecretKeySpec key1 = kms.deriveContentKey("content-1");
        SecretKeySpec key2 = kms.deriveContentKey("content-2");

        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()),
                "Different content IDs should produce different keys");
    }

    @Test
    void derivedKeyIs16Bytes() {
        KmsDataKeyService kms = service();
        SecretKeySpec key = kms.deriveContentKey("content-1");

        assertEquals(16, key.getEncoded().length);
    }

    @Test
    void kmsDisabledWhenKeyEmpty() {
        KmsDataKeyService kms = service();
        assertFalse(kms.isKmsEnabled());
    }
}
