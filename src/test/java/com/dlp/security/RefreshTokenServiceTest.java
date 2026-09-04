package com.dlp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private RedisTemplate<String, Object> redisTemplate;
    private RefreshTokenService service;
    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new RefreshTokenService(redisTemplate, 7200000);
    }

    @Test
    void generateRefreshTokenReturnsUniqueTokens() {
        String token1 = service.generateRefreshToken("user@test.com");
        String token2 = service.generateRefreshToken("user@test.com");

        assertNotNull(token1);
        assertEquals(32, token1.length());
        verify(valueOps, times(2)).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void validateRefreshTokenReturnsTrueForMatchingToken() {
        String token = "matching-token";
        when(valueOps.get("dl:refresh:user@test.com")).thenReturn(token);

        assertTrue(service.validateRefreshToken("user@test.com", token));
    }

    @Test
    void validateRefreshTokenReturnsFalseForWrongToken() {
        when(valueOps.get("dl:refresh:user@test.com")).thenReturn("stored-token");

        assertFalse(service.validateRefreshToken("user@test.com", "wrong-token"));
    }

    @Test
    void validateRefreshTokenReturnsFalseWhenNoTokenStored() {
        when(valueOps.get("dl:refresh:user@test.com")).thenReturn(null);

        assertFalse(service.validateRefreshToken("user@test.com", "any-token"));
    }

    @Test
    void revokeRefreshTokenDeletesKey() {
        service.revokeRefreshToken("user@test.com");
        verify(redisTemplate).delete("dl:refresh:user@test.com");
    }
}
