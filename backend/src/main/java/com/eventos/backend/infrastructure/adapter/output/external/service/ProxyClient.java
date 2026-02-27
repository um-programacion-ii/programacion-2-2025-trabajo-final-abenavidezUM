package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.eventos.backend.dto.proxy.ProxyEstadoAsientoResponseDTO;
import com.eventos.backend.dto.proxy.ProxyMapaAsientosResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente para consumir la API REST del servicio proxy
 * El proxy consulta el Redis de cátedra para obtener estado de asientos en tiempo real
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyClient {

    @Qualifier("proxyRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${proxy.url}")
    private String proxyUrl;

    @Value("${proxy.base-path}")
    private String proxyBasePath;

    /**
     * Obtiene el estado de un asiento específico desde Redis de cátedra vía proxy
     * 
     * @param eventoId ID del evento
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return Estado del asiento o null si no disponible
     */
    public ProxyEstadoAsientoResponseDTO obtenerEstadoAsiento(Long eventoId, Integer fila, Integer columna) {
        String url = String.format("%s%s/asientos/estado/%d/%d/%d", 
                proxyUrl, proxyBasePath, eventoId, fila, columna);
        
        try {
            log.debug("Consultando estado de asiento {}:{} evento {} en proxy", fila, columna, eventoId);
            
            ResponseEntity<ProxyEstadoAsientoResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ProxyEstadoAsientoResponseDTO.class
            );
            
            ProxyEstadoAsientoResponseDTO resultado = response.getBody();
            if (resultado != null) {
                log.debug("Estado asiento {}:{} evento {}: {}", 
                        fila, columna, eventoId, resultado.getEstado());
            }
            
            return resultado;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Asiento {}:{} evento {} no encontrado en proxy", fila, columna, eventoId);
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error HTTP al consultar asiento en proxy: {} - {}", 
                    e.getStatusCode(), e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            log.error("Proxy no disponible: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error inesperado al consultar asiento en proxy", e);
            return null;
        }
    }

    /**
     * Obtiene el mapa completo de asientos de un evento desde Redis de cátedra vía proxy
     * 
     * @param eventoId ID del evento
     * @return Mapa de asientos con sus estados o null si no disponible
     */
    public ProxyMapaAsientosResponseDTO obtenerMapaAsientos(Long eventoId) {
        String url = String.format("%s%s/asientos/mapa/%d", 
                proxyUrl, proxyBasePath, eventoId);
        
        try {
            log.info("Consultando mapa de asientos evento {} en proxy", eventoId);
            
            ResponseEntity<ProxyMapaAsientosResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ProxyMapaAsientosResponseDTO.class
            );
            
            ProxyMapaAsientosResponseDTO resultado = response.getBody();
            if (resultado != null) {
                log.info("Mapa de asientos evento {} obtenido: {} asientos", 
                        eventoId, resultado.getTotalAsientos());
            }
            
            return resultado;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Evento {} no encontrado en proxy", eventoId);
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error HTTP al consultar mapa en proxy: {} - {}", 
                    e.getStatusCode(), e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            log.error("Proxy no disponible: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error inesperado al consultar mapa en proxy", e);
            return null;
        }
    }

    /**
     * Obtiene el resumen de asientos por estado desde Redis de cátedra vía proxy
     * 
     * @param eventoId ID del evento
     * @return Mapa con conteo por estado o null si no disponible
     */
    public Map<String, Long> obtenerResumenAsientos(Long eventoId) {
        String url = String.format("%s%s/asientos/resumen/%d", 
                proxyUrl, proxyBasePath, eventoId);
        
        try {
            log.debug("Consultando resumen de asientos evento {} en proxy", eventoId);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("resumen")) {
                @SuppressWarnings("unchecked")
                Map<String, Long> resumen = (Map<String, Long>) body.get("resumen");
                log.debug("Resumen asientos evento {} obtenido", eventoId);
                return resumen;
            }
            
            return null;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Evento {} no encontrado en proxy", eventoId);
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error HTTP al consultar resumen en proxy: {} - {}", 
                    e.getStatusCode(), e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            log.error("Proxy no disponible: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error inesperado al consultar resumen en proxy", e);
            return null;
        }
    }

    /**
     * Verifica si el servicio proxy está disponible
     * 
     * @return true si está disponible, false si no
     */
    public boolean isProxyAvailable() {
        String url = String.format("%s%s/health", proxyUrl, proxyBasePath);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            Map<String, Object> health = response.getBody();
            boolean available = health != null && "UP".equals(health.get("status"));
            
            if (available) {
                log.info("Proxy está disponible");
            } else {
                log.warn("Proxy está degradado");
            }
            
            return available;
            
        } catch (Exception e) {
            log.warn("Proxy NO está disponible: {}", e.getMessage());
            return false;
        }
    }
}

