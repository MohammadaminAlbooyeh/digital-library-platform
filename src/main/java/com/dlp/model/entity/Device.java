package com.dlp.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_fingerprint", nullable = false, unique = true)
    private String deviceFingerprint;

    @Column(name = "is_registered")
    private boolean registered = true;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @PrePersist
    void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}

