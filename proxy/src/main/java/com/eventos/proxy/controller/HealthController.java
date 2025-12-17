package com.eventos.proxy.controller;

import com.eventos.proxy.client.CatedraApiClient;
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

    private final CatedraApiClient catedraApiClient;

    /**
     * Health check básico del proxy
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "proxy-service");
        
        // Verificar conectividad con API de cátedra
        boolean catedraAvailable = catedraApiClient.isApiAvailable();
        health.put("catedra_api", catedraAvailable ? "UP" : "DOWN");
        
        return ResponseEntity.ok(health);
    }
}

