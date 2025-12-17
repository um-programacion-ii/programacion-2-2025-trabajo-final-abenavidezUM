package com.eventos.proxy.client;

import com.eventos.proxy.dto.EventoCompletoDTO;
import com.eventos.proxy.dto.EventoResumenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    /**
     * Obtiene la lista de eventos resumidos
     */
    public List<EventoResumenDTO> getEventosResumidos() {
        String url = buildUrl("/api/endpoints/v1/eventos-resumidos");
        log.debug("GET {}", url);
        
        try {
            ResponseEntity<List<EventoResumenDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventoResumenDTO>>() {}
            );
            
            log.info("Obtenidos {} eventos resumidos", response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error al obtener eventos resumidos: {}", e.getMessage());
            throw new RuntimeException("Error al consultar eventos resumidos de la cátedra", e);
        }
    }

    /**
     * Obtiene la lista de eventos completos
     */
    public List<EventoCompletoDTO> getEventosCompletos() {
        String url = buildUrl("/api/endpoints/v1/eventos");
        log.debug("GET {}", url);
        
        try {
            ResponseEntity<List<EventoCompletoDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventoCompletoDTO>>() {}
            );
            
            log.info("Obtenidos {} eventos completos", response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error al obtener eventos completos: {}", e.getMessage());
            throw new RuntimeException("Error al consultar eventos completos de la cátedra", e);
        }
    }

    /**
     * Obtiene el detalle de un evento específico por ID
     */
    public EventoCompletoDTO getEventoById(Long id) {
        String url = buildUrl("/api/endpoints/v1/evento/" + id);
        log.debug("GET {}", url);
        
        try {
            ResponseEntity<EventoCompletoDTO> response = restTemplate.getForEntity(
                url,
                EventoCompletoDTO.class
            );
            
            log.info("Obtenido evento ID: {}", id);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error al obtener evento {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al consultar evento " + id + " de la cátedra", e);
        }
    }
}

