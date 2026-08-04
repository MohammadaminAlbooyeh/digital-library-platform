package com.dlp.repository;

import com.dlp.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(Long userId, String status);
}

