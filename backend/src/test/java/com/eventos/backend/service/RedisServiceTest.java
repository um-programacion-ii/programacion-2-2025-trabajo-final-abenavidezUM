package com.eventos.backend.infrastructure.adapter.output.external.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    private static final String TEST_KEY = "test:key";
    private static final String TEST_VALUE = "test-value";

    @BeforeEach
    void cleanup() {
        // Limpiar keys de prueba
        redisService.delete(TEST_KEY);
    }

    @Test
    void contextLoads() {
        assertNotNull(redisService);
    }

    @Test
    void testSaveAndGet() {
        // Given
        String key = TEST_KEY;
        String value = TEST_VALUE;

        // When
        redisService.save(key, value);
        Object retrieved = redisService.get(key);

        // Then
        assertNotNull(retrieved);
        assertEquals(value, retrieved);
    }

    @Test
    void testSaveWithCustomTTL() {
        // Given
        String key = TEST_KEY;
        String value = TEST_VALUE;

        // When
        redisService.save(key, value, 10, TimeUnit.SECONDS);
        Long ttl = redisService.getExpiration(key);

        // Then
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 10);
    }

    @Test
    void testExists() {
        // Given
        String key = TEST_KEY;

        // When
        redisService.save(key, TEST_VALUE);

        // Then
        assertTrue(redisService.exists(key));
        assertFalse(redisService.exists("nonexistent:key"));
    }

    @Test
    void testDelete() {
        // Given
        String key = TEST_KEY;
        redisService.save(key, TEST_VALUE);
        assertTrue(redisService.exists(key));

        // When
        redisService.delete(key);

        // Then
        assertFalse(redisService.exists(key));
    }

    @Test
    void testGetWithClass() {
        // Given
        String key = TEST_KEY;
        String value = TEST_VALUE;
        redisService.save(key, value);

        // When
        String retrieved = redisService.get(key, String.class);

        // Then
        assertNotNull(retrieved);
        assertEquals(value, retrieved);
    }

    @Test
    void testIncrement() {
        // Given
        String key = "test:counter";
        redisService.delete(key);

        // When
        Long count1 = redisService.increment(key);
        Long count2 = redisService.increment(key);

        // Then
        assertEquals(1L, count1);
        assertEquals(2L, count2);

        // Cleanup
        redisService.delete(key);
    }

    @Test
    void testDecrement() {
        // Given
        String key = "test:counter";
        redisService.delete(key);
        redisService.increment(key);
        redisService.increment(key);

        // When
        Long count = redisService.decrement(key);

        // Then
        assertEquals(1L, count);

        // Cleanup
        redisService.delete(key);
    }
}

