package com.eventos.backend.infrastructure.adapter.output.external;

import com.eventos.backend.domain.ports.output.RedisCachePort;
import com.eventos.backend.infrastructure.adapter.output.external.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Adaptador para caché en Redis
 * Implementa el puerto de salida usando RedisService
 */
@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements RedisCachePort {

    private final RedisService redisService;

    @Override
    public void save(String key, Object value, long ttl, TimeUnit timeUnit) {
        redisService.save(key, value, ttl, timeUnit);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return redisService.get(key, clazz);
    }

    @Override
    public void delete(String key) {
        redisService.delete(key);
    }

    @Override
    public boolean exists(String key) {
        return redisService.exists(key);
    }
}

