package com.dlp.repository;

import com.dlp.model.entity.UserLibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLibraryRepository extends JpaRepository<UserLibraryItem, Long> {

    List<UserLibraryItem> findByUserId(Long userId);

    Optional<UserLibraryItem> findByUserIdAndContentIdAndContentType(Long userId, Long contentId, String contentType);

    boolean existsByUserIdAndContentIdAndContentType(Long userId, Long contentId, String contentType);
}

