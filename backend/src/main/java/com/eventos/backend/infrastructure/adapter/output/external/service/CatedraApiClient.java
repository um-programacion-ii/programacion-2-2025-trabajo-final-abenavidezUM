package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.eventos.backend.dto.catedra.*;
import com.eventos.backend.domain.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatedraApiClient {

    private final RestTemplate restTemplate;

    @Value("${catedra.api.url}")
    private String catedraApiUrl;

    /**
     * Registrar un nuevo usuario en el servicio de cátedra
     */
    public CatedraRegistroResponseDTO registrarUsuario(CatedraRegistroRequestDTO request) {
        String url = catedraApiUrl + "/api/v1/agregar_usuario";
        
        try {
            log.info("Registrando usuario en cátedra: {}", request.getUsername());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatedraRegistroRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CatedraRegistroResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CatedraRegistroResponseDTO.class
            );
            
            log.info("Usuario registrado exitosamente en cátedra: {}", request.getUsername());
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al registrar usuario en cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al registrar usuario en servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al registrar usuario en cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Realizar login en el servicio de cátedra
     */
    public CatedraLoginResponseDTO login(CatedraLoginRequestDTO request) {
        String url = catedraApiUrl + "/api/authenticate";
        
        try {
            log.info("Realizando login en cátedra: {}", request.getUsername());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatedraLoginRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CatedraLoginResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CatedraLoginResponseDTO.class
            );
            
            log.info("Login exitoso en cátedra: {}", request.getUsername());
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al hacer login en cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al autenticar con servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al hacer login en cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Obtener listado resumido de eventos desde cátedra
     */
    public List<CatedraEventoResumenDTO> obtenerEventosResumidos() {
        String url = catedraApiUrl + "/api/endpoints/v1/eventos-resumidos";
        
        try {
            log.info("Obteniendo eventos resumidos desde cátedra");
            
            ResponseEntity<List<CatedraEventoResumenDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CatedraEventoResumenDTO>>() {}
            );
            
            List<CatedraEventoResumenDTO> eventos = response.getBody();
            log.info("Obtenidos {} eventos resumidos desde cátedra", eventos != null ? eventos.size() : 0);
            return eventos;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al obtener eventos resumidos desde cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al obtener eventos desde servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener eventos resumidos desde cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Obtener listado completo de eventos desde cátedra
     */
    public List<CatedraEventoCompletoDTO> obtenerEventosCompletos() {
        String url = catedraApiUrl + "/api/endpoints/v1/eventos";
        
        try {
            log.info("Obteniendo eventos completos desde cátedra");
            
            ResponseEntity<List<CatedraEventoCompletoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CatedraEventoCompletoDTO>>() {}
            );
            
            List<CatedraEventoCompletoDTO> eventos = response.getBody();
            log.info("Obtenidos {} eventos completos desde cátedra", eventos != null ? eventos.size() : 0);
            return eventos;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al obtener eventos completos desde cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al obtener eventos desde servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener eventos completos desde cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Obtener detalle de un evento específico desde cátedra
     */
    public CatedraEventoCompletoDTO obtenerEventoPorId(Long eventoId) {
        String url = catedraApiUrl + "/api/endpoints/v1/evento/" + eventoId;
        
        try {
            log.info("Obteniendo detalle del evento {} desde cátedra", eventoId);
            
            ResponseEntity<CatedraEventoCompletoDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    CatedraEventoCompletoDTO.class
            );
            
            log.info("Detalle del evento {} obtenido desde cátedra", eventoId);
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al obtener detalle del evento {} desde cátedra: {}", eventoId, e.getMessage());
            throw new BadRequestException("Error al obtener evento desde servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener detalle del evento {} desde cátedra", eventoId, e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Bloquear asientos en el servicio de cátedra
     */
    public CatedraBloquearAsientosResponseDTO bloquearAsientos(CatedraBloquearAsientosRequestDTO request) {
        String url = catedraApiUrl + "/api/endpoints/v1/bloquear-asientos";
        
        try {
            log.info("Bloqueando {} asientos para evento {} en cátedra", 
                    request.getAsientos().size(), request.getEventoId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatedraBloquearAsientosRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CatedraBloquearAsientosResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CatedraBloquearAsientosResponseDTO.class
            );
            
            CatedraBloquearAsientosResponseDTO responseBody = response.getBody();
            if (responseBody != null && responseBody.getResultado()) {
                log.info("Asientos bloqueados exitosamente para evento {}", request.getEventoId());
            } else {
                log.warn("No se pudieron bloquear todos los asientos para evento {}: {}", 
                        request.getEventoId(), 
                        responseBody != null ? responseBody.getDescripcion() : "Sin descripción");
            }
            
            return responseBody;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al bloquear asientos en cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al bloquear asientos en servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al bloquear asientos en cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Realizar venta de asientos en el servicio de cátedra
     */
    public CatedraRealizarVentaResponseDTO realizarVenta(CatedraRealizarVentaRequestDTO request) {
        String url = catedraApiUrl + "/api/endpoints/v1/realizar-venta";
        
        try {
            log.info("Realizando venta de {} asientos para evento {} en cátedra", 
                    request.getAsientos().size(), request.getEventoId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatedraRealizarVentaRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CatedraRealizarVentaResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CatedraRealizarVentaResponseDTO.class
            );
            
            CatedraRealizarVentaResponseDTO responseBody = response.getBody();
            if (responseBody != null && responseBody.getResultado()) {
                log.info("Venta realizada exitosamente para evento {}, venta ID: {}", 
                        request.getEventoId(), responseBody.getVentaId());
            } else {
                log.warn("Venta rechazada para evento {}: {}", 
                        request.getEventoId(), 
                        responseBody != null ? responseBody.getDescripcion() : "Sin descripción");
            }
            
            return responseBody;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al realizar venta en cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al realizar venta en servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al realizar venta en cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Listar todas las ventas del usuario actual desde cátedra
     */
    public List<CatedraVentaResumenDTO> listarVentas() {
        String url = catedraApiUrl + "/api/endpoints/v1/listar-ventas";
        
        try {
            log.info("Obteniendo listado de ventas desde cátedra");
            
            ResponseEntity<List<CatedraVentaResumenDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CatedraVentaResumenDTO>>() {}
            );
            
            List<CatedraVentaResumenDTO> ventas = response.getBody();
            log.info("Obtenidas {} ventas desde cátedra", ventas != null ? ventas.size() : 0);
            return ventas;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al obtener listado de ventas desde cátedra: {}", e.getMessage());
            throw new BadRequestException("Error al obtener ventas desde servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener listado de ventas desde cátedra", e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }

    /**
     * Obtener detalle de una venta específica desde cátedra
     */
    public CatedraVentaDetalleDTO obtenerVentaPorId(Long ventaId) {
        String url = catedraApiUrl + "/api/endpoints/v1/listar-venta/" + ventaId;
        
        try {
            log.info("Obteniendo detalle de la venta {} desde cátedra", ventaId);
            
            ResponseEntity<CatedraVentaDetalleDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    CatedraVentaDetalleDTO.class
            );
            
            log.info("Detalle de la venta {} obtenido desde cátedra", ventaId);
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error al obtener detalle de la venta {} desde cátedra: {}", ventaId, e.getMessage());
            throw new BadRequestException("Error al obtener venta desde servicio de cátedra: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener detalle de la venta {} desde cátedra", ventaId, e);
            throw new RuntimeException("Error al comunicarse con servicio de cátedra", e);
        }
    }
}

