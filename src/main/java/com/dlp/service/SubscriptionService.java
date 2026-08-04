package com.dlp.service;

import com.dlp.exception.SubscriptionExpiredException;
import com.dlp.model.dto.SubscriptionPlanDTO;
import com.dlp.model.entity.Subscription;
import com.dlp.model.entity.User;
import com.dlp.model.enums.SubscriptionStatus;
import com.dlp.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    // Static plan catalogue for the demo platform.
    private static final Map<Long, SubscriptionPlanDTO> PLANS = Map.of(
            1L, plan(1L, "Basic", "1 device, ebooks only", new BigDecimal("9.99"), 1, false),
            2L, plan(2L, "Plus", "3 devices, ebooks and audiobooks", new BigDecimal("14.99"), 3, true),
            3L, plan(3L, "Premium", "5 devices, everything included", new BigDecimal("19.99"), 5, true));

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    private static SubscriptionPlanDTO plan(long id, String name, String desc, BigDecimal price, int devices, boolean audio) {
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(desc);
        dto.setMonthlyPrice(price);
        dto.setMaxDevices(devices);
        dto.setAudioBooksIncluded(audio);
        return dto;
    }

    public List<SubscriptionPlanDTO> listPlans() {
        return List.copyOf(PLANS.values());
    }

    public SubscriptionPlanDTO getPlan(Long id) {
        SubscriptionPlanDTO plan = PLANS.get(id);
        if (plan == null) {
            throw new IllegalArgumentException("Unknown subscription plan: " + id);
        }
        return plan;
    }

    public Subscription subscribe(User user, Long planId) {
        SubscriptionPlanDTO plan = getPlan(planId);
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan.getName());
        subscription.setMonthlyPrice(plan.getMonthlyPrice());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        return subscriptionRepository.save(subscription);
    }

    public Subscription getActiveSubscription(User user) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusOrderByEndDateDesc(user.getId(), SubscriptionStatus.ACTIVE.name())
                .orElseThrow(() -> new SubscriptionExpiredException("No active subscription"));
    }

    public boolean hasActiveSubscription(User user) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusOrderByEndDateDesc(user.getId(), SubscriptionStatus.ACTIVE.name())
                .isPresent();
    }

    public void cancel(User user) {
        getActiveSubscription(user).setStatus(SubscriptionStatus.CANCELLED);
        // save handled elsewhere; update directly here
        subscriptionRepository.save(getActiveSubscription(user));
    }

    public List<Subscription> listSubscriptions(User user) {
        return subscriptionRepository.findByUserId(user.getId());
    }
}

