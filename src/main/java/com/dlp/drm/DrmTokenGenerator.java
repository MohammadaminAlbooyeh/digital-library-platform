package com.dlp.drm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class DrmTokenGenerator {

    @Value("${app.drm.encryption-key}")
    private String signingKey;

    public String generateToken(String payload, String deviceFingerprint) {
        try {
            String base = payload + "|" + deviceFingerprint;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(base.getBytes(StandardCharsets.UTF_8));
            String sigHex = HexFormat.of().formatHex(signature);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(base.getBytes(StandardCharsets.UTF_8));
            return encoded + "." + sigHex;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate DRM token", e);
        }
    }

    public boolean verifyToken(String token, String payload, String deviceFingerprint) {
        if (token == null || !token.contains(".")) {
            return false;
        }
        int dot = token.lastIndexOf('.');
        String base = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        String expected = generateToken(payload, deviceFingerprint);
        int expectedDot = expected.lastIndexOf('.');
        return MessageDigest.isEqual(
                expected.substring(expectedDot + 1).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    public String payloadFor(long userId, long contentId, long expiryEpochSeconds) {
        return userId + ":" + contentId + ":" + expiryEpochSeconds;
    }
}

