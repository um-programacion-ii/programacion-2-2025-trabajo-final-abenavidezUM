package com.eventos.proxy.controller;

import com.eventos.proxy.service.CatedraRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller de diagnóstico para inspeccionar Redis de cátedra
 * 
 * SOLO PARA DESARROLLO - Permite ver qué keys y datos hay en Redis
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/redis")
@RequiredArgsConstructor
public class RedisDebugController {

    @Qualifier("catedraRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    
    private final CatedraRedisService catedraRedisService;

    /**
     * GET /api/debug/redis/keys
     * 
     * Lista TODAS las keys que existen en Redis de cátedra
     */
    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> getAllKeys(
            @RequestParam(defaultValue = "*") String pattern) {
        
        log.info("GET /api/debug/redis/keys?pattern={}", pattern);
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("pattern", pattern);
            response.put("total", keys != null ? keys.size() : 0);
            response.put("keys", keys != null ? new ArrayList<>(keys) : List.of());
            
            log.info("Encontradas {} keys con patrón '{}'", response.get("total"), pattern);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener keys de Redis: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/debug/redis/evento/{eventoId}
     * 
     * Inspecciona TODAS las estructuras de datos para un evento específico
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<Map<String, Object>> inspectEvento(
            @PathVariable Long eventoId) {
        
        log.info("GET /api/debug/redis/evento/{} - Inspeccionando evento", eventoId);
        
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("eventoId", eventoId);
            
            // 1. Verificar Hash: "evento:{id}:asientos"
            String hashKey = String.format("evento:%d:asientos", eventoId);
            Map<Object, Object> hashData = redisTemplate.opsForHash().entries(hashKey);
            response.put("hashKey", hashKey);
            response.put("hashExists", !hashData.isEmpty());
            response.put("hashSize", hashData.size());
            response.put("hashData", hashData);
            
            // 2. Buscar keys individuales: "evento:{id}:asiento:*"
            String individualPattern = String.format("evento:%d:asiento:*", eventoId);
            Set<String> individualKeys = redisTemplate.keys(individualPattern);
            response.put("individualPattern", individualPattern);
            response.put("individualKeysFound", individualKeys != null ? individualKeys.size() : 0);
            response.put("individualKeys", individualKeys != null ? new ArrayList<>(individualKeys) : List.of());
            
            // 3. Si hay keys individuales, leer algunas
            if (individualKeys != null && !individualKeys.isEmpty()) {
                Map<String, Object> sampleData = new LinkedHashMap<>();
                int count = 0;
                for (String key : individualKeys) {
                    if (count++ >= 10) break; // Solo primeros 10
                    Object value = redisTemplate.opsForValue().get(key);
                    sampleData.put(key, value);
                }
                response.put("sampleIndividualData", sampleData);
            }
            
            // 4. Buscar cualquier otra key relacionada al evento
            String anyPattern = String.format("*evento*%d*", eventoId);
            Set<String> anyKeys = redisTemplate.keys(anyPattern);
            response.put("anyPattern", anyPattern);
            response.put("anyKeysFound", anyKeys != null ? anyKeys.size() : 0);
            response.put("anyKeys", anyKeys != null ? new ArrayList<>(anyKeys) : List.of());
            
            // 5. Resumen
            response.put("summary", Map.of(
                "hasHashData", !hashData.isEmpty(),
                "hasIndividualKeys", individualKeys != null && !individualKeys.isEmpty(),
                "totalDataPoints", hashData.size() + (individualKeys != null ? individualKeys.size() : 0)
            ));
            
            log.info("Evento {} - Hash: {} asientos, Keys individuales: {}", 
                    eventoId, hashData.size(), 
                    individualKeys != null ? individualKeys.size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al inspeccionar evento {}: {}", eventoId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "eventoId", eventoId
            ));
        }
    }

    /**
     * GET /api/debug/redis/all-eventos
     * 
     * Encuentra TODOS los eventos que tienen algún tipo de datos en Redis
     */
    @GetMapping("/all-eventos")
    public ResponseEntity<Map<String, Object>> findAllEventos() {
        
        log.info("GET /api/debug/redis/all-eventos - Buscando todos los eventos");
        
        try {
            // Buscar todas las keys que mencionen "evento"
            Set<String> allKeys = redisTemplate.keys("*evento*");
            
            // Extraer IDs de eventos
            Set<Long> eventoIds = new TreeSet<>();
            if (allKeys != null) {
                for (String key : allKeys) {
                    // Intentar extraer ID del evento de la key
                    // Formatos posibles: "evento:1:asientos", "evento:1:asiento:2:3", etc.
                    String[] parts = key.split(":");
                    for (String part : parts) {
                        try {
                            Long id = Long.parseLong(part);
                            eventoIds.add(id);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            response.put("eventosEncontrados", eventoIds);
            response.put("totalEventos", eventoIds.size());
            response.put("allKeys", allKeys != null ? new ArrayList<>(allKeys) : List.of());
            
            log.info("Encontrados {} eventos en Redis: {}", eventoIds.size(), eventoIds);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al buscar eventos en Redis: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/debug/redis/ping
     * 
     * Verifica conectividad con Redis de cátedra
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        
        log.info("GET /api/debug/redis/ping - Verificando conexión");
        
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            
            Map<String, Object> response = Map.of(
                "status", "UP",
                "ping", pong,
                "message", "Conectado a Redis de cátedra"
            );
            
            log.info("Redis PING: {}", pong);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al hacer PING a Redis: {}", e.getMessage(), e);
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }
}

