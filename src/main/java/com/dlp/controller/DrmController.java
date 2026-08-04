package com.dlp.controller;

import com.dlp.model.entity.Device;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.DrmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drm")
public class DrmController {

    private final DrmService drmService;
    private final CurrentUserProvider currentUserProvider;

    public DrmController(DrmService drmService, CurrentUserProvider currentUserProvider) {
        this.drmService = drmService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/devices")
    public ResponseEntity<Device> registerDevice(@RequestBody Map<String, String> body) {
        var user = currentUserProvider.currentUser();
        Device device = drmService.registerDevice(user,
                body.getOrDefault("deviceName", "unnamed"),
                body.getOrDefault("deviceType", "reader"),
                body.get("deviceFingerprint"));
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @GetMapping("/devices")
    public List<Device> devices() {
        return drmService.listDevices(currentUserProvider.currentUser());
    }

    @DeleteMapping("/devices")
    public ResponseEntity<Void> unregister(@RequestParam String deviceFingerprint) {
        drmService.unregisterDevice(currentUserProvider.currentUser(), deviceFingerprint);
        return ResponseEntity.noContent().build();
    }
}
