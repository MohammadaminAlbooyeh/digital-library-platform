package com.dlp.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class DownloadTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.delivery.streaming-chunk-size}")
    private long chunkSize;

    public DownloadTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long userId, Long contentId, String contentType, Duration ttl) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = "dl:download:" + token;
        String value = userId + ":" + contentId + ":" + contentType;
        redisTemplate.opsForValue().set(key, value, ttl);
        return token;
    }

    public boolean isValidToken(String token, Long userId) {
        String key = "dl:download:" + token;
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return false;
        }
        String[] parts = raw.toString().split(":");
        return parts.length >= 1 && parts[0].equals(String.valueOf(userId));
    }

    public void revokeToken(String token) {
        redisTemplate.delete("dl:download:" + token);
    }
}

