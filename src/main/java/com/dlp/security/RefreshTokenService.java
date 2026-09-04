package com.dlp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final long expirationMs;

    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate,
                               @Value("${app.jwt.refresh-expiration-ms:604800000}") long expirationMs) {
        this.redisTemplate = redisTemplate;
        this.expirationMs = expirationMs;
    }

    public String generateRefreshToken(String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = "dl:refresh:" + username;
        redisTemplate.opsForValue().set(key, token, Duration.ofMillis(expirationMs));
        return token;
    }

    public boolean validateRefreshToken(String username, String token) {
        String key = "dl:refresh:" + username;
        Object stored = redisTemplate.opsForValue().get(key);
        return token.equals(stored);
    }

    public void revokeRefreshToken(String username) {
        redisTemplate.delete("dl:refresh:" + username);
    }
}
