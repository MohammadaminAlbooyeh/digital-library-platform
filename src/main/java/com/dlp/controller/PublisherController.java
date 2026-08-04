package com.dlp.controller;

import com.dlp.model.entity.Publisher;
import com.dlp.service.PublisherRoyaltyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherRoyaltyService royaltyService;

    public PublisherController(PublisherRoyaltyService royaltyService) {
        this.royaltyService = royaltyService;
    }

    @GetMapping
    public List<Publisher> list() {
        return royaltyService.listPublishers();
    }

    @GetMapping("/{id}/royalties")
    public ResponseEntity<Map<String, Object>> royalties(@PathVariable Long id) {
        BigDecimal total = royaltyService.computeRoyalties(id);
        return ResponseEntity.ok(Map.of("publisherId", id, "totalRoyalties", total));
    }
}

