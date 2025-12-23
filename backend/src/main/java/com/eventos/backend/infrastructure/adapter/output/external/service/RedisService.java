package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // TTL por defecto de 30 minutos
    private static final long DEFAULT_TTL_MINUTES = 30;

    /**
     * Guardar un valor en Redis con TTL por defecto
     */
    public void save(String key, Object value) {
        save(key, value, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Guardar un valor en Redis con TTL personalizado
     */
    public void save(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Saved key: {} with TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error saving key: {}", key, e);
            throw new RuntimeException("Error saving to Redis", e);
        }
    }

    /**
     * Obtener un valor de Redis
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved key: {} with value present: {}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Error getting key: {}", key, e);
            return null;
        }
    }

    /**
     * Obtener un valor de Redis con conversión automática usando ObjectMapper
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        
        try {
            // Si el valor ya es del tipo correcto, devolverlo directamente
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            
            // Convertir usando ObjectMapper (maneja LinkedHashMap → DTO)
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Error converting value for key: {} to class: {}", key, clazz.getName(), e);
            return null;
        }
    }

    /**
     * Verificar si una key existe
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking existence of key: {}", key, e);
            return false;
        }
    }

    /**
     * Eliminar una key
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
        }
    }

    /**
     * Actualizar el TTL de una key existente
     */
    public boolean updateExpiration(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            log.debug("Updated expiration for key: {} to {} {}", key, timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error updating expiration for key: {}", key, e);
            return false;
        }
    }

    /**
     * Obtener el tiempo restante de expiración de una key
     */
    public Long getExpiration(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting expiration for key: {}", key, e);
            return -2L; // -2 indica que la key no existe
        }
    }

    /**
     * Guardar un valor sin expiración
     */
    public void savePermanent(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Saved permanent key: {}", key);
        } catch (Exception e) {
            log.error("Error saving permanent key: {}", key, e);
            throw new RuntimeException("Error saving permanent value to Redis", e);
        }
    }

    /**
     * Incrementar un contador
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key: {}", key, e);
            return null;
        }
    }

    /**
     * Decrementar un contador
     */
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Error decrementing key: {}", key, e);
            return null;
        }
    }

    /**
     * Renovar expiración con el TTL por defecto
     */
    public boolean renewExpiration(String key) {
        return updateExpiration(key, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
    }
}

