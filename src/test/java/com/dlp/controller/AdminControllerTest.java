package com.dlp.controller;

import com.dlp.model.entity.Transaction;
import com.dlp.model.entity.User;
import com.dlp.repository.TransactionRepository;
import com.dlp.repository.UserRepository;
import com.dlp.service.PublisherRoyaltyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private PublisherRoyaltyService royaltyService;

    @MockBean
    private com.dlp.security.JwtService jwtService;

    @MockBean
    private com.dlp.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void usersReturnsAllUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@test.com");
        user.setName("Admin");
        user.setRole("ADMIN");
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.com"));
    }

    @Test
    void transactionsReturnsAllTransactions() throws Exception {
        Transaction txn = new Transaction();
        txn.setId(1L);
        txn.setAmount(new BigDecimal("29.99"));
        txn.setCurrency("USD");
        when(transactionRepository.findAll()).thenReturn(List.of(txn));

        mockMvc.perform(get("/api/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(29.99));
    }

    @Test
    void statsReturnsCounts() throws Exception {
        when(userRepository.count()).thenReturn(42L);
        when(transactionRepository.count()).thenReturn(17L);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(42))
                .andExpect(jsonPath("$.transactions").value(17));
    }

    @Test
    void royaltiesReturnsTotal() throws Exception {
        when(royaltyService.computeRoyalties(5L)).thenReturn(new BigDecimal("1234.56"));

        mockMvc.perform(get("/api/admin/royalties/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publisherId").value(5))
                .andExpect(jsonPath("$.totalRoyalties").value(1234.56));
    }
}
