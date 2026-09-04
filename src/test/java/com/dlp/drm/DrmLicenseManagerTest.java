package com.dlp.drm;

import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import com.dlp.kms.KmsDataKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DrmLicenseManagerTest {

    private DrmLicenseManager licenseManager;
    private DeviceRegistrationService deviceService;

    @BeforeEach
    void setUp() {
        DrmTokenGenerator tokenGenerator = new DrmTokenGenerator();
        ReflectionTestUtils.setField(tokenGenerator, "signingKey", "my-strong-signing-key");
        KmsDataKeyService kmsService = mock(KmsDataKeyService.class);
        ContentEncryptionService encryptionService = new ContentEncryptionService(
                "0123456789abcdef", kmsService);

        deviceService = mock(DeviceRegistrationService.class);
        when(deviceService.fingerprint(any())).thenReturn("device-hash");
        when(deviceService.registerDevice(any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Device d = new Device();
                    d.setId(3L);
                    return d;
                });

        licenseManager = new DrmLicenseManager(tokenGenerator, encryptionService, deviceService);
        ReflectionTestUtils.setField(licenseManager, "licenseTtlHours", 48L);
    }

    private User user() {
        User user = new User();
        user.setId(10L);
        return user;
    }

    @Test
    void issueLicenseReturnsEncryptedString() {
        String license = licenseManager.issueLicense(user(), 5L, "fp");
        assertNotNull(license);
        assertFalse(license.isBlank());
    }

    @Test
    void issuedLicenseIsVerifiable() {
        String license = licenseManager.issueLicense(user(), 5L, "fp");
        assertTrue(licenseManager.verifyLicense(license, user(), 5L, "fp"));
    }

    @Test
    void licenseForDifferentContentFails() {
        String license = licenseManager.issueLicense(user(), 5L, "fp");
        assertFalse(licenseManager.verifyLicense(license, user(), 9L, "fp"));
    }

    @Test
    void malformedLicenseFails() {
        assertFalse(licenseManager.verifyLicense("not-a-valid-license", user(), 5L, "fp"));
    }
}

