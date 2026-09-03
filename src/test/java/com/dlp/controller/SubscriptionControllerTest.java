package com.dlp.controller;

import com.dlp.model.dto.SubscriptionPlanDTO;
import com.dlp.model.entity.Subscription;
import com.dlp.model.entity.User;
import com.dlp.model.enums.SubscriptionStatus;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private com.dlp.security.JwtService jwtService;

    @MockBean
    private com.dlp.security.CustomUserDetailsService customUserDetailsService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user@test.com");
        currentUser.setName("Test User");
    }

    @Test
    void plansReturnsAllPlans() throws Exception {
        SubscriptionPlanDTO plan = new SubscriptionPlanDTO();
        plan.setId(1L);
        plan.setName("Premium");
        plan.setDescription("Full access to all content");
        plan.setMonthlyPrice(new BigDecimal("9.99"));
        plan.setMaxDevices(5);
        plan.setAudioBooksIncluded(true);

        when(subscriptionService.listPlans()).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/subscriptions/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Premium"))
                .andExpect(jsonPath("$[0].monthlyPrice").value(9.99));
    }

    @Test
    void subscribeReturnsCreatedSubscription() throws Exception {
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPlan("Premium");
        sub.setStatus(SubscriptionStatus.ACTIVE);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(subscriptionService.subscribe(currentUser, 1L)).thenReturn(sub);

        mockMvc.perform(post("/api/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.plan").value("Premium"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void subscribeRejectsMissingPlanId() throws Exception {
        mockMvc.perform(post("/api/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mineReturnsUserSubscriptions() throws Exception {
        Subscription sub = new Subscription();
        sub.setId(10L);
        sub.setPlan("Premium");
        sub.setStatus(SubscriptionStatus.ACTIVE);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(subscriptionService.listSubscriptions(currentUser)).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/subscriptions/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void cancelReturnsNoContent() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        doNothing().when(subscriptionService).cancel(currentUser);

        mockMvc.perform(post("/api/subscriptions/cancel"))
                .andExpect(status().isNoContent());
    }
}
