package com.eventos.proxy.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para consumir la API del servidor de cátedra
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatedraApiClient {

    @Qualifier("catedraRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${catedra.api.url}")
    private String baseUrl;

    /**
     * Construye la URL completa para un endpoint
     */
    private String buildUrl(String endpoint) {
        return baseUrl + endpoint;
    }

    /**
     * Health check básico para verificar conectividad con la API
     */
    public boolean isApiAvailable() {
        try {
            // Intentar hacer un GET a un endpoint simple
            String url = buildUrl("/api/endpoints/v1/eventos-resumidos");
            restTemplate.getForEntity(url, String.class);
            log.info("Conexión con API de cátedra verificada");
            return true;
        } catch (Exception e) {
            log.error("Error al conectar con API de cátedra: {}", e.getMessage());
            return false;
        }
    }
}

