package com.dlp.drm;

import com.dlp.exception.DrmViolationException;
import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import com.dlp.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
public class DeviceRegistrationService {

    private final DeviceRepository deviceRepository;
    private final int maxDevicesPerUser = 5;

    public DeviceRegistrationService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device registerDevice(User user, String deviceName, String deviceType, String rawFingerprint) {
        String fingerprint = fingerprint(rawFingerprint);
        return deviceRepository.findByDeviceFingerprint(fingerprint)
                .map(existing -> {
                    existing.setDeviceName(deviceName);
                    existing.setDeviceType(deviceType);
                    existing.setRegistered(true);
                    return deviceRepository.save(existing);
                })
                .orElseGet(() -> {
                    long current = deviceRepository.countByUserId(user.getId());
                    if (current >= maxDevicesPerUser) {
                        throw new DrmViolationException("Maximum number of devices reached for this account");
                    }
                    Device device = new Device();
                    device.setUser(user);
                    device.setDeviceName(deviceName);
                    device.setDeviceType(deviceType);
                    device.setDeviceFingerprint(fingerprint);
                    device.setRegistered(true);
                    return deviceRepository.save(device);
                });
    }

    public void unregisterDevice(User user, String deviceFingerprint) {
        String fingerprint = fingerprint(deviceFingerprint);
        deviceRepository.findByDeviceFingerprint(fingerprint)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .ifPresent(d -> {
                    d.setRegistered(false);
                    deviceRepository.save(d);
                });
    }

    public List<Device> listDevices(User user) {
        return deviceRepository.findByUserId(user.getId());
    }

    public String fingerprint(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash device fingerprint", e);
        }
    }
}

