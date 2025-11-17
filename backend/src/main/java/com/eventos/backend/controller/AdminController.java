package com.eventos.backend.controller;

import com.eventos.backend.service.EventoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final EventoSyncService eventoSyncService;

    /**
     * POST /admin/sync/eventos
     * Sincronizar todos los eventos manualmente
     */
    @PostMapping("/sync/eventos")
    public ResponseEntity<Map<String, Object>> sincronizarEventos() {
        log.info("Sincronización manual de eventos solicitada");
        
        try {
            int eventosSincronizados = eventoSyncService.sincronizarTodos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventosSincronizados", eventosSincronizados);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", "Sincronización completada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en sincronización manual de eventos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * POST /admin/sync/eventos/{idExterno}
     * Sincronizar un evento específico
     */
    @PostMapping("/sync/eventos/{idExterno}")
    public ResponseEntity<Map<String, Object>> sincronizarEvento(@PathVariable Long idExterno) {
        log.info("Sincronización manual del evento {} solicitada", idExterno);
        
        try {
            var evento = eventoSyncService.sincronizarEvento(idExterno);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", evento != null);
            response.put("eventoId", evento != null ? evento.getId() : null);
            response.put("timestamp", LocalDateTime.now());
            response.put("mensaje", evento != null ? "Evento sincronizado exitosamente" : "Evento no encontrado en cátedra");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en sincronización manual del evento {}: {}", idExterno, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /admin/sync/eventos/status
     * Obtener información sobre la última sincronización
     */
    @GetMapping("/sync/eventos/status")
    public ResponseEntity<Map<String, Object>> obtenerStatusSincronizacion() {
        try {
            LocalDateTime ultimaSincronizacion = eventoSyncService.obtenerUltimaSincronizacion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("ultimaSincronizacion", ultimaSincronizacion);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener status de sincronización: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

