package com.eventos.proxy.controller;

import com.eventos.proxy.client.CatedraApiClient;
import com.eventos.proxy.dto.EventoCompletoDTO;
import com.eventos.proxy.dto.EventoResumenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para endpoints de consulta de eventos
 * Expone la API de eventos consumiendo el servicio de cátedra
 */
@Slf4j
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final CatedraApiClient catedraApiClient;

    /**
     * GET /api/eventos/resumidos
     * 
     * Obtiene la lista de eventos con datos resumidos (sin integrantes, dirección, imagen ni dimensiones)
     * Útil para vistas de listado simple.
     * 
     * @return Lista de eventos resumidos
     */
    @GetMapping("/resumidos")
    public ResponseEntity<List<EventoResumenDTO>> getEventosResumidos() {
        log.info("GET /api/eventos/resumidos - Consultando eventos resumidos");
        
        try {
            List<EventoResumenDTO> eventos = catedraApiClient.getEventosResumidos();
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            log.error("Error al obtener eventos resumidos: {}", e.getMessage());
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
    }

    /**
     * GET /api/eventos
     * 
     * Obtiene la lista de eventos con todos los datos (incluye integrantes, dirección, imagen y dimensiones)
     * 
     * @return Lista de eventos completos
     */
    @GetMapping
    public ResponseEntity<List<EventoCompletoDTO>> getEventosCompletos() {
        log.info("GET /api/eventos - Consultando eventos completos");
        
        try {
            List<EventoCompletoDTO> eventos = catedraApiClient.getEventosCompletos();
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            log.error("Error al obtener eventos completos: {}", e.getMessage());
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
    }

    /**
     * GET /api/eventos/{id}
     * 
     * Obtiene el detalle completo de un evento específico por su ID
     * 
     * @param id ID del evento
     * @return Detalle completo del evento
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoCompletoDTO> getEventoById(@PathVariable Long id) {
        log.info("GET /api/eventos/{} - Consultando detalle de evento", id);
        
        try {
            EventoCompletoDTO evento = catedraApiClient.getEventoById(id);
            
            if (evento == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(evento);
        } catch (Exception e) {
            log.error("Error al obtener evento {}: {}", id, e.getMessage());
            
            // Si el error es 404, devolver 404; de lo contrario 503
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
    }
}

