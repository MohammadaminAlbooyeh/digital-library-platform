package com.dlp.recommendation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class RecommendationClient {

    private final RestClient restClient;

    public RecommendationClient(@Value("${app.recommendation.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getRecommendationsForUser(Long userId, int limit) {
        Map<String, Object> body = restClient.get()
                .uri("/recommendations/{userId}?limit={limit}", userId, limit)
                .retrieve()
                .body(Map.class);
        if (body == null || !body.containsKey("contentIds")) {
            return List.of();
        }
        return (List<Long>) body.get("contentIds");
    }
}

