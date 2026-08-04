package com.dlp.repository;

import com.dlp.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceFingerprint(String fingerprint);

    List<Device> findByUserId(Long userId);

    long countByUserId(Long userId);
}

