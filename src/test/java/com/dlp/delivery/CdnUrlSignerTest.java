package com.dlp.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CdnUrlSignerTest {

    private CdnUrlSigner signer;

    @BeforeEach
    void setUp() {
        signer = new CdnUrlSigner();
        ReflectionTestUtils.setField(signer, "cdnBaseUrl", "https://cdn.example.com");
        ReflectionTestUtils.setField(signer, "signingKey", "test-signing-key");
    }

    @Test
    void signUrlProducesExpiryAndSignature() {
        String url = signer.signUrl("content/books/1/file.pdf", Duration.ofHours(1));
        assertTrue(url.startsWith("https://cdn.example.com/content/books/1/file.pdf"));
        assertTrue(url.contains("expires="));
        assertTrue(url.contains("signature="));
    }

    @Test
    void verifyValidUrlIsTrue() {
        String url = signer.signUrl("content/books/1/file.pdf", Duration.ofMinutes(5));
        assertTrue(signer.verifyUrl(url, Duration.ofHours(1)));
    }

    @Test
    void verifyTamperedUrlIsFalse() {
        String url = signer.signUrl("content/books/1/file.pdf", Duration.ofMinutes(5));
        String tampered = url.replace("file.pdf", "other.pdf");
        assertFalse(signer.verifyUrl(tampered, Duration.ofHours(1)));
    }

    @Test
    void verifyUrlWithoutSignatureIsFalse() {
        assertFalse(signer.verifyUrl("https://cdn.example.com/content/books/1/file.pdf?expires=1234", Duration.ofHours(1)));
    }
}

