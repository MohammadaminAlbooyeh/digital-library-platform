package com.dlp.model.entity;

import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_library")
@Getter
@Setter
public class UserLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private AccessType accessType = AccessType.OWNED;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt;

    @PrePersist
    void onCreate() {
        acquiredAt = LocalDateTime.now();
    }
}

