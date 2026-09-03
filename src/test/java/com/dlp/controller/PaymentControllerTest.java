package com.dlp.controller;

import com.dlp.model.entity.Transaction;
import com.dlp.model.entity.User;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

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
    void purchaseReturnsTransaction() throws Exception {
        Transaction txn = new Transaction();
        txn.setId(99L);
        txn.setUser(currentUser);
        txn.setContentType("BOOK");
        txn.setContentId(5L);
        txn.setAmount(new BigDecimal("29.99"));
        txn.setCurrency("USD");
        txn.setPaypalPaymentId("PAYPAL-123");
        txn.setStatus("COMPLETED");

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(paymentService.purchase(currentUser, com.dlp.model.enums.ContentType.BOOK, 5L, "PAYPAL-123"))
                .thenReturn(txn);

        mockMvc.perform(post("/api/payments/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"BOOK\",\"contentId\":5,\"paypalPaymentId\":\"PAYPAL-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.amount").value(29.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.paypalPaymentId").value("PAYPAL-123"));
    }

    @Test
    void purchaseRejectsMissingContentId() throws Exception {
        mockMvc.perform(post("/api/payments/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"BOOK\"}"))
                .andExpect(status().is4xxClientError());
    }
}
