package com.dlp.controller;

import com.dlp.model.entity.Transaction;
import com.dlp.model.entity.User;
import com.dlp.repository.TransactionRepository;
import com.dlp.repository.UserRepository;
import com.dlp.service.PublisherRoyaltyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PublisherRoyaltyService royaltyService;

    public AdminController(UserRepository userRepository,
                           TransactionRepository transactionRepository,
                           PublisherRoyaltyService royaltyService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.royaltyService = royaltyService;
    }

    @GetMapping("/users")
    public List<User> users() {
        return userRepository.findAll();
    }

    @GetMapping("/transactions")
    public List<Transaction> transactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "users", userRepository.count(),
                "transactions", transactionRepository.count()));
    }

    @GetMapping("/royalties/{publisherId}")
    public ResponseEntity<Map<String, Object>> royalties(@PathVariable Long publisherId) {
        return ResponseEntity.ok(Map.of(
                "publisherId", publisherId,
                "totalRoyalties", royaltyService.computeRoyalties(publisherId)));
    }
}

