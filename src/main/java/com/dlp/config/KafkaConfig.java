package com.dlp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String READING_EVENTS_TOPIC = "reading-events";
    public static final String PURCHASE_EVENTS_TOPIC = "purchase-events";

    @Bean
    public ObjectMapper kafkaObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public NewTopic readingEventsTopic() {
        return TopicBuilder.name(READING_EVENTS_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic purchaseEventsTopic() {
        return TopicBuilder.name(PURCHASE_EVENTS_TOPIC).partitions(3).replicas(1).build();
    }
}

