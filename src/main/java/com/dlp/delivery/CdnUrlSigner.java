package com.dlp.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class CdnUrlSigner {

    @Value("${app.aws.s3.cdn-base-url}")
    private String cdnBaseUrl;

    @Value("${app.drm.encryption-key}")
    private String signingKey;

    public String signUrl(String resourcePath, Duration ttl) {
        String expires = String.valueOf(Instant.now().plus(ttl).getEpochSecond());
        String toSign = resourcePath + "?expires=" + expires;
        String signature = sign(toSign);
        return cdnBaseUrl + "/" + resourcePath + "?expires=" + expires + "&signature=" + signature;
    }

    public boolean verifyUrl(String signedUrl, Duration maxTtl) {
        try {
            int queryIdx = signedUrl.indexOf('?');
            if (queryIdx < 0) {
                return false;
            }
            String base = signedUrl.substring(0, queryIdx);
            String[] params = signedUrl.substring(queryIdx + 1).split("&");
            String expires = null;
            String signature = null;
            for (String p : params) {
                String[] kv = p.split("=", 2);
                if (kv.length != 2) {
                    continue;
                }
                if (kv[0].equals("expires")) {
                    expires = kv[1];
                } else if (kv[0].equals("signature")) {
                    signature = kv[1];
                }
            }
            if (expires == null || signature == null) {
                return false;
            }
            long expiryEpoch = Long.parseLong(expires);
            if (Instant.ofEpochSecond(expiryEpoch).isBefore(Instant.now())) {
                return false;
            }
            if (Duration.between(Instant.now(), Instant.ofEpochSecond(expiryEpoch)).compareTo(maxTtl) > 0) {
                return false;
            }
            String resourcePath = base.substring(cdnBaseUrl.length() + 1);
            String toSign = resourcePath + "?expires=" + expires;
            return sign(toSign).equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String sign(String toSign) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign URL", e);
        }
    }
}

