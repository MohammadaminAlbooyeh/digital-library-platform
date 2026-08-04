package com.dlp.controller;

import com.dlp.model.dto.SubscriptionPlanDTO;
import com.dlp.model.entity.Subscription;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserProvider currentUserProvider;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  CurrentUserProvider currentUserProvider) {
        this.subscriptionService = subscriptionService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/plans")
    public List<SubscriptionPlanDTO> plans() {
        return subscriptionService.listPlans();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Long> body) {
        Long planId = body.get("planId");
        if (planId == null) {
            return ResponseEntity.badRequest().build();
        }
        var user = currentUserProvider.currentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.subscribe(user, planId));
    }

    @GetMapping("/mine")
    public List<Subscription> mine() {
        return subscriptionService.listSubscriptions(currentUserProvider.currentUser());
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel() {
        subscriptionService.cancel(currentUserProvider.currentUser());
        return ResponseEntity.noContent().build();
    }
}

