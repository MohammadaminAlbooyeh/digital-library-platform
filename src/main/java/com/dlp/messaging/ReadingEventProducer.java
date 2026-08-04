package com.dlp.messaging;

import com.dlp.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReadingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReadingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReadingProgress(Long userId, Long contentId, Long positionSeconds, double progressPercent) {
        Map<String, Object> event = Map.of(
                "userId", userId,
                "contentId", contentId,
                "positionSeconds", positionSeconds,
                "progressPercent", progressPercent,
                "timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.READING_EVENTS_TOPIC, String.valueOf(userId), event);
    }
}

