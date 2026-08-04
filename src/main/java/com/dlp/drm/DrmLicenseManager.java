package com.dlp.drm;

import com.dlp.exception.DrmViolationException;
import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class DrmLicenseManager {

    private final DrmTokenGenerator tokenGenerator;
    private final ContentEncryptionService encryptionService;
    private final DeviceRegistrationService deviceService;

    @Value("${app.drm.license-ttl-hours}")
    private long licenseTtlHours;

    public DrmLicenseManager(DrmTokenGenerator tokenGenerator,
                             ContentEncryptionService encryptionService,
                             DeviceRegistrationService deviceService) {
        this.tokenGenerator = tokenGenerator;
        this.encryptionService = encryptionService;
        this.deviceService = deviceService;
    }

    public String issueLicense(User user, long contentId, String rawDeviceFingerprint) {
        Device device = deviceService.registerDevice(user, "active", "reader", rawDeviceFingerprint);
        String fingerprint = deviceService.fingerprint(rawDeviceFingerprint);
        long expiry = Instant.now().plus(Duration.ofHours(licenseTtlHours)).getEpochSecond();
        String payload = tokenGenerator.payloadFor(user.getId(), contentId, expiry);
        String license = tokenGenerator.generateToken(payload, fingerprint);
        try {
            return encryptionService.encryptToBase64(license);
        } catch (Exception e) {
            throw new DrmViolationException("Failed to encrypt license: " + e.getMessage());
        }
    }

    public boolean verifyLicense(String encryptedLicense, User user, long contentId, String rawDeviceFingerprint) {
        String fingerprint = deviceService.fingerprint(rawDeviceFingerprint);
        try {
            String license = encryptionService.decryptFromBase64(encryptedLicense);
            if (license == null || license.indexOf('.') < 0) {
                return false;
            }
            int dot = license.lastIndexOf('.');
            String encodedBase = license.substring(0, dot);
            String receivedToken = license;
            String decoded = new String(
                    java.util.Base64.getUrlDecoder().decode(encodedBase), java.nio.charset.StandardCharsets.UTF_8);
            int bar = decoded.indexOf('|');
            if (bar < 0) {
                return false;
            }
            String payload = decoded.substring(0, bar);
            String decodedFingerprint = decoded.substring(bar + 1);
            if (!decodedFingerprint.equals(fingerprint)) {
                return false;
            }
            String expectedPrefix = user.getId() + ":" + contentId + ":";
            if (!payload.startsWith(expectedPrefix)) {
                return false;
            }
            long expiry = extractExpiry(payload);
            if (Instant.ofEpochSecond(expiry).isBefore(Instant.now())) {
                throw new DrmViolationException("License has expired");
            }
            String expected = tokenGenerator.generateToken(payload, fingerprint);
            return expected.equals(receivedToken);
        } catch (DrmViolationException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    private long extractExpiry(String payload) {
        String[] parts = payload.split(":");
        return parts.length >= 3 ? Long.parseLong(parts[2]) : 0L;
    }
}

