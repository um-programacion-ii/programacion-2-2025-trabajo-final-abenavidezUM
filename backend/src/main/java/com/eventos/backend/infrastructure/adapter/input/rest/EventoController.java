package com.eventos.backend.infrastructure.adapter.input.rest;

import com.eventos.backend.dto.EventoDetalleDTO;
import com.eventos.backend.dto.EventoResumenDTO;
import com.eventos.backend.application.service.EventoServiceImpl;
import com.eventos.backend.infrastructure.adapter.output.external.service.EventoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gestión de eventos
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoServiceImpl eventoService;
    private final EventoSyncService eventoSyncService;

    // ==================== ENDPOINTS PÚBLICOS ====================

    /**
     * GET /api/eventos/public
     * Obtener listado de eventos activos con paginación (público)
     */
    @GetMapping("/public")
    public ResponseEntity<Page<EventoResumenDTO>> getAllEventosPublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/eventos/public - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EventoResumenDTO> eventos = eventoService.findAllActive(pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/public/{id}
     * Obtener detalle de un evento por ID (público)
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<EventoDetalleDTO> getEventoByIdPublic(@PathVariable Long id) {
        log.info("GET /api/eventos/public/{}", id);
        EventoDetalleDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    /**
     * GET /api/eventos/public/externo/{idExterno}
     * Obtener detalle de un evento por ID externo (público)
     */
    @GetMapping("/public/externo/{idExterno}")
    public ResponseEntity<EventoDetalleDTO> getEventoByIdExternoPublic(@PathVariable Long idExterno) {
        log.info("GET /api/eventos/public/externo/{}", idExterno);
        EventoDetalleDTO evento = eventoService.findByIdExterno(idExterno);
        return ResponseEntity.ok(evento);
    }

    /**
     * GET /api/eventos/public/future
     * Obtener eventos futuros (público)
     */
    @GetMapping("/public/future")
    public ResponseEntity<Page<EventoResumenDTO>> getFutureEventsPublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/public/future - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.findFutureEvents(pageable);
        return ResponseEntity.ok(eventos);
    }

    // ==================== ENDPOINTS AUTENTICADOS ====================

    /**
     * GET /api/eventos
     * Obtener listado de eventos activos con paginación (autenticado)
     */
    @GetMapping
    public ResponseEntity<Page<EventoResumenDTO>> getAllEventos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/eventos - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EventoResumenDTO> eventos = eventoService.findAllActive(pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/{id}
     * Obtener detalle de un evento por ID (autenticado)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> getEventoById(@PathVariable Long id) {
        log.info("GET /api/eventos/{}", id);
        EventoDetalleDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    /**
     * GET /api/eventos/externo/{idExterno}
     * Obtener detalle de un evento por ID externo
     */
    @GetMapping("/externo/{idExterno}")
    public ResponseEntity<EventoDetalleDTO> getEventoByIdExterno(@PathVariable Long idExterno) {
        log.info("GET /api/eventos/externo/{}", idExterno);
        EventoDetalleDTO evento = eventoService.findByIdExterno(idExterno);
        return ResponseEntity.ok(evento);
    }

    /**
     * GET /api/eventos/search
     * Buscar eventos por título
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventoResumenDTO>> searchEventos(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/search - q: '{}', page: {}, size: {}", q, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.searchByTitulo(q, pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/future
     * Obtener eventos futuros
     */
    @GetMapping("/future")
    public ResponseEntity<Page<EventoResumenDTO>> getFutureEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/future - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.findFutureEvents(pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/past
     * Obtener eventos pasados
     */
    @GetMapping("/past")
    public ResponseEntity<Page<EventoResumenDTO>> getPastEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/past - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.findPastEvents(pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/tipo/{tipoId}
     * Obtener eventos por tipo
     */
    @GetMapping("/tipo/{tipoId}")
    public ResponseEntity<Page<EventoResumenDTO>> getEventosByTipo(
            @PathVariable Long tipoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/tipo/{} - page: {}, size: {}", tipoId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.findByTipoEvento(tipoId, pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/date-range
     * Obtener eventos en un rango de fechas
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<EventoResumenDTO>> getEventosByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/date-range - desde: {}, hasta: {}", desde, hasta);
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.findByDateRange(desde, hasta, pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/advanced
     * Búsqueda avanzada con múltiples filtros
     */
    @GetMapping("/advanced")
    public ResponseEntity<Page<EventoResumenDTO>> searchAdvanced(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long tipoEventoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/eventos/advanced - titulo: '{}', tipo: {}, desde: {}, hasta: {}", 
                titulo, tipoEventoId, fechaDesde, fechaHasta);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventoResumenDTO> eventos = eventoService.searchAdvanced(
                titulo, tipoEventoId, fechaDesde, fechaHasta, pageable);
        return ResponseEntity.ok(eventos);
    }

    /**
     * GET /api/eventos/count
     * Obtener cantidad de eventos activos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countActiveEvents() {
        log.info("GET /api/eventos/count");
        Long count = eventoService.countActiveEvents();
        return ResponseEntity.ok(Map.of("totalEventosActivos", count));
    }

    // ==================== ENDPOINTS ADMINISTRATIVOS ====================

    /**
     * POST /api/eventos/sync
     * Sincronizar todos los eventos con el servicio de cátedra (requiere autenticación)
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sincronizarEventos() {
        log.info("POST /api/eventos/sync - Sincronización manual solicitada");
        
        try {
            int eventosSincronizados = eventoSyncService.sincronizarTodos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventosSincronizados", eventosSincronizados);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", "Sincronización completada exitosamente");
            
            log.info("Sincronización manual completada: {} eventos", eventosSincronizados);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en sincronización manual: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", "Error al sincronizar eventos");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * POST /api/eventos/sync/{idExterno}
     * Sincronizar un evento específico por su ID externo
     */
    @PostMapping("/sync/{idExterno}")
    public ResponseEntity<Map<String, Object>> sincronizarEvento(@PathVariable Long idExterno) {
        log.info("POST /api/eventos/sync/{} - Sincronización de evento específico", idExterno);
        
        try {
            var evento = eventoSyncService.sincronizarEvento(idExterno);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", evento != null);
            response.put("eventoId", evento != null ? evento.getId() : null);
            response.put("eventoIdExterno", idExterno);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", evento != null 
                    ? "Evento sincronizado exitosamente" 
                    : "Evento no encontrado en servicio de cátedra");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al sincronizar evento {}: {}", idExterno, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("eventoIdExterno", idExterno);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", "Error al sincronizar evento");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /api/eventos/sync/status
     * Obtener información sobre la última sincronización
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> obtenerStatusSincronizacion() {
        log.info("GET /api/eventos/sync/status");
        
        try {
            LocalDateTime ultimaSincronizacion = eventoSyncService.obtenerUltimaSincronizacion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("ultimaSincronizacion", ultimaSincronizacion);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", ultimaSincronizacion != null 
                    ? "Última sincronización registrada" 
                    : "No hay sincronizaciones registradas");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener status de sincronización: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", "Error al obtener status");
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

