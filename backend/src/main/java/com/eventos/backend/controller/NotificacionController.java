package com.eventos.backend.controller;

import com.eventos.backend.dto.notificacion.NotificacionAsientoDTO;
import com.eventos.backend.dto.notificacion.NotificacionEventoDTO;
import com.eventos.backend.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para recibir notificaciones desde el servicio proxy
 * 
 * El proxy envía notificaciones cuando detecta cambios en el servidor de cátedra
 * a través de Kafka (eventos nuevos, asientos bloqueados/vendidos, etc.)
 */
@Slf4j
@RestController
@RequestMapping("/admin/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * POST /admin/notificaciones/eventos/evento
     * 
     * Recibe notificaciones de cambios en eventos desde el proxy
     * 
     * Tipos de notificaciones:
     * - NUEVO_EVENTO: Se creó un nuevo evento
     * - EVENTO_ACTUALIZADO: Se modificó un evento existente
     * - EVENTO_CANCELADO: Se canceló un evento
     * 
     * @param notificacion Datos del evento que cambió
     * @return Confirmación de recepción
     */
    @PostMapping("/eventos/evento")
    public ResponseEntity<Map<String, String>> recibirNotificacionEvento(
            @RequestBody NotificacionEventoDTO notificacion) {
        
        log.info("POST /admin/notificaciones/eventos/evento - Tipo: {}, Evento: {}", 
                notificacion.getTipo(), notificacion.getEventoId());
        
        try {
            notificacionService.procesarNotificacionEvento(notificacion);
            
            return ResponseEntity.ok(Map.of(
                    "status", "received",
                    "message", "Notificación de evento procesada exitosamente",
                    "eventoId", notificacion.getEventoId().toString()
            ));
        } catch (Exception e) {
            log.error("Error al procesar notificación de evento: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error al procesar notificación: " + e.getMessage()
            ));
        }
    }

    /**
     * POST /admin/notificaciones/eventos/asiento
     * 
     * Recibe notificaciones de cambios en asientos desde el proxy
     * 
     * Tipos de notificaciones:
     * - ASIENTO_BLOQUEADO: Se bloqueó un asiento
     * - ASIENTO_VENDIDO: Se vendió un asiento
     * - ASIENTO_LIBERADO: Se liberó un asiento
     * 
     * @param notificacion Datos del asiento que cambió
     * @return Confirmación de recepción
     */
    @PostMapping("/eventos/asiento")
    public ResponseEntity<Map<String, String>> recibirNotificacionAsiento(
            @RequestBody NotificacionAsientoDTO notificacion) {
        
        log.info("POST /admin/notificaciones/eventos/asiento - Tipo: {}, Evento: {}, Asiento: {}:{}", 
                notificacion.getTipo(), notificacion.getEventoId(), 
                notificacion.getFila(), notificacion.getColumna());
        
        try {
            notificacionService.procesarNotificacionAsiento(notificacion);
            
            return ResponseEntity.ok(Map.of(
                    "status", "received",
                    "message", "Notificación de asiento procesada exitosamente",
                    "eventoId", notificacion.getEventoId().toString(),
                    "asiento", notificacion.getFila() + ":" + notificacion.getColumna()
            ));
        } catch (Exception e) {
            log.error("Error al procesar notificación de asiento: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error al procesar notificación: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /admin/notificaciones/health
     * 
     * Health check del endpoint de notificaciones
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-endpoint",
                "message", "Listo para recibir notificaciones del proxy"
        ));
    }
}

