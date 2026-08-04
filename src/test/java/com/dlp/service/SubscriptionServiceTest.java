package com.dlp.service;

import com.dlp.exception.SubscriptionExpiredException;
import com.dlp.model.dto.SubscriptionPlanDTO;
import com.dlp.model.entity.Subscription;
import com.dlp.model.entity.User;
import com.dlp.model.enums.SubscriptionStatus;
import com.dlp.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository repository;

    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(repository);
    }

    @Test
    void listsPlans() {
        List<SubscriptionPlanDTO> plans = service.listPlans();
        assertEquals(3, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getName().equals("Premium")));
    }

    @Test
    void unknownPlanThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.getPlan(999L));
    }

    @Test
    void subscribeCreatesActiveSubscription() {
        User user = new User();
        user.setId(1L);
        when(repository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription sub = service.subscribe(user, 2L);
        assertEquals("Plus", sub.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals(new BigDecimal("14.99"), sub.getMonthlyPrice());
        assertNotNull(sub.getEndDate());
    }

    @Test
    void noActiveSubscriptionThrows() {
        when(repository.findFirstByUserIdAndStatusOrderByEndDateDesc(eq(1L), eq("ACTIVE")))
                .thenReturn(Optional.empty());
        User user = new User();
        user.setId(1L);
        assertThrows(SubscriptionExpiredException.class, () -> service.getActiveSubscription(user));
    }

    @Test
    void activeSubscriptionReturnedWhenPresent() {
        Subscription sub = new Subscription();
        sub.setStatus(SubscriptionStatus.ACTIVE);
        when(repository.findFirstByUserIdAndStatusOrderByEndDateDesc(eq(1L), eq("ACTIVE")))
                .thenReturn(Optional.of(sub));
        User user = new User();
        user.setId(1L);
        assertEquals(sub, service.getActiveSubscription(user));
    }
}

