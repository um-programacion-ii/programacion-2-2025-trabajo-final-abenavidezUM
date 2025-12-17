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
        List<EventoResumenDTO> eventos = catedraApiClient.getEventosResumidos();
        return ResponseEntity.ok(eventos);
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
        List<EventoCompletoDTO> eventos = catedraApiClient.getEventosCompletos();
        return ResponseEntity.ok(eventos);
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
        EventoCompletoDTO evento = catedraApiClient.getEventoById(id);
        return ResponseEntity.ok(evento);
    }
}

