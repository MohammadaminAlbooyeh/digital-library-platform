package com.dlp.controller;

import com.dlp.model.entity.Transaction;
import com.dlp.model.enums.ContentType;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserProvider currentUserProvider;

    public PaymentController(PaymentService paymentService, CurrentUserProvider currentUserProvider) {
        this.paymentService = paymentService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/purchase")
    public ResponseEntity<Transaction> purchase(@RequestBody Map<String, Object> body) {
        ContentType contentType = ContentType.valueOf(String.valueOf(body.get("contentType")).toUpperCase());
        Long contentId = Long.valueOf(String.valueOf(body.get("contentId")));
        String paypalId = (String) body.get("paypalPaymentId");
        var user = currentUserProvider.currentUser();
        Transaction txn = paymentService.purchase(user, contentType, contentId, paypalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }
}

