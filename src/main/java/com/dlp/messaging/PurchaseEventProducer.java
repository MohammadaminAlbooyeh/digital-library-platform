package com.dlp.messaging;

import com.dlp.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PurchaseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PurchaseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPurchase(Long userId, String contentType, Long contentId, BigDecimal amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);
        event.put("contentType", contentType);
        event.put("contentId", contentId);
        event.put("amount", amount);
        event.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.PURCHASE_EVENTS_TOPIC, String.valueOf(userId), event);
    }
}

