package com.dlp.service;

import com.dlp.drm.DeviceRegistrationService;
import com.dlp.drm.DrmLicenseManager;
import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrmService {

    private final DrmLicenseManager licenseManager;
    private final DeviceRegistrationService deviceService;

    public DrmService(DrmLicenseManager licenseManager, DeviceRegistrationService deviceService) {
        this.licenseManager = licenseManager;
        this.deviceService = deviceService;
    }

    public String issueLicense(User user, long contentId, String deviceFingerprint) {
        return licenseManager.issueLicense(user, contentId, deviceFingerprint);
    }

    public boolean verifyLicense(String license, User user, long contentId, String deviceFingerprint) {
        return licenseManager.verifyLicense(license, user, contentId, deviceFingerprint);
    }

    public Device registerDevice(User user, String deviceName, String deviceType, String fingerprint) {
        return deviceService.registerDevice(user, deviceName, deviceType, fingerprint);
    }

    public List<Device> listDevices(User user) {
        return deviceService.listDevices(user);
    }

    public void unregisterDevice(User user, String fingerprint) {
        deviceService.unregisterDevice(user, fingerprint);
    }
}

