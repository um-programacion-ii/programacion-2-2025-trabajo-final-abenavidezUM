package com.eventos.proxy.client;

import com.eventos.proxy.dto.BloquearAsientosRequestDTO;
import com.eventos.proxy.dto.BloquearAsientosResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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

    /**
     * Bloquea asientos para un evento
     * 
     * @param request Petición con eventoId y lista de asientos a bloquear
     * @return Respuesta con resultado del bloqueo y estado de cada asiento
     */
    public BloquearAsientosResponseDTO bloquearAsientos(BloquearAsientosRequestDTO request) {
        String url = buildUrl("/api/endpoints/v1/bloquear-asientos");
        log.debug("POST {} - Bloqueando {} asientos para evento {}", 
                url, request.getAsientos().size(), request.getEventoId());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<BloquearAsientosRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BloquearAsientosResponseDTO> response = restTemplate.postForEntity(
                url,
                entity,
                BloquearAsientosResponseDTO.class
            );
            
            BloquearAsientosResponseDTO resultado = response.getBody();
            
            if (resultado != null) {
                log.info("Bloqueo de asientos - Evento: {}, Resultado: {}, Mensaje: {}", 
                        request.getEventoId(), 
                        resultado.getResultado(), 
                        resultado.getDescripcion());
            }
            
            return resultado;
        } catch (RestClientException e) {
            log.error("Error al bloquear asientos para evento {}: {}", 
                    request.getEventoId(), e.getMessage());
            throw new RuntimeException("Error al bloquear asientos en la cátedra", e);
        }
    }
}

