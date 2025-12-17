package com.eventos.proxy.controller;

import com.eventos.proxy.service.BackendNotificationService;
import com.eventos.proxy.service.CatedraRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para health checks y verificación de estado del servicio
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final CatedraRedisService catedraRedisService;
    private final BackendNotificationService backendNotificationService;

    /**
     * Health check del proxy verificando conexiones a Redis de cátedra y backend
     * 
     * GET /proxy/api/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "proxy-service");
        
        // Verificar conectividad con Redis de cátedra
        boolean redisAvailable = catedraRedisService.isRedisAvailable();
        health.put("catedra_redis", redisAvailable ? "UP" : "DOWN");
        
        // Verificar conectividad con backend
        boolean backendAvailable = backendNotificationService.isBackendAvailable();
        health.put("backend", backendAvailable ? "UP" : "DOWN");
        
        // Estado general
        boolean allUp = redisAvailable && backendAvailable;
        health.put("status", allUp ? "UP" : "DEGRADED");
        
        return ResponseEntity.ok(health);
    }
}

