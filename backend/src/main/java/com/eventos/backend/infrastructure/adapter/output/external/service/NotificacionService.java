package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.eventos.backend.application.service.SesionCompraServiceImpl;
import com.eventos.backend.dto.notificacion.NotificacionAsientoDTO;
import com.eventos.backend.dto.notificacion.NotificacionEventoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para procesar notificaciones recibidas desde el proxy
 * 
 * El proxy envía notificaciones cuando detecta cambios en:
 * - Eventos (nuevo, actualizado, cancelado) desde Kafka de cátedra
 * - Asientos (bloqueado, vendido, liberado) desde Kafka de cátedra
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final EventoSyncService eventoSyncService;
    private final SesionCompraServiceImpl sesionCompraService;

    /**
     * Procesa una notificación de cambio en un evento
     * 
     * @param notificacion Datos del evento que cambió
     */
    public void procesarNotificacionEvento(NotificacionEventoDTO notificacion) {
        log.info("Procesando notificación de evento: {} - Evento ID: {}", 
                notificacion.getTipo(), notificacion.getEventoId());
        
        try {
            switch (notificacion.getTipo()) {
                case "NUEVO_EVENTO":
                    procesarNuevoEvento(notificacion);
                    break;
                case "EVENTO_ACTUALIZADO":
                    procesarEventoActualizado(notificacion);
                    break;
                case "EVENTO_CANCELADO":
                    procesarEventoCancelado(notificacion);
                    break;
                default:
                    log.warn("Tipo de notificación de evento desconocido: {}", 
                            notificacion.getTipo());
            }
        } catch (Exception e) {
            log.error("Error al procesar notificación de evento {}: {}", 
                    notificacion.getEventoId(), e.getMessage(), e);
        }
    }

    /**
     * Procesa una notificación de cambio en un asiento
     * 
     * @param notificacion Datos del asiento que cambió
     */
    public void procesarNotificacionAsiento(NotificacionAsientoDTO notificacion) {
        log.info("Procesando notificación de asiento: {} - Evento: {}, Asiento: {}:{}", 
                notificacion.getTipo(), notificacion.getEventoId(), 
                notificacion.getFila(), notificacion.getColumna());
        
        try {
            switch (notificacion.getTipo()) {
                case "ASIENTO_BLOQUEADO":
                    procesarAsientoBloqueado(notificacion);
                    break;
                case "ASIENTO_VENDIDO":
                    procesarAsientoVendido(notificacion);
                    break;
                case "ASIENTO_LIBERADO":
                    procesarAsientoLiberado(notificacion);
                    break;
                default:
                    log.warn("Tipo de notificación de asiento desconocido: {}", 
                            notificacion.getTipo());
            }
        } catch (Exception e) {
            log.error("Error al procesar notificación de asiento {}:{} evento {}: {}", 
                    notificacion.getFila(), notificacion.getColumna(), 
                    notificacion.getEventoId(), e.getMessage(), e);
        }
    }

    /**
     * Procesa la notificación de un nuevo evento
     */
    private void procesarNuevoEvento(NotificacionEventoDTO notificacion) {
        log.info("Nuevo evento detectado: {} - {}", 
                notificacion.getEventoId(), notificacion.getNombre());
        
        // Sincronizar eventos desde cátedra para incluir el nuevo
        try {
            eventoSyncService.sincronizarTodos();
            log.info("Sincronización de eventos completada tras nuevo evento");
        } catch (Exception e) {
            log.error("Error al sincronizar eventos tras nuevo evento: {}", e.getMessage());
        }
    }

    /**
     * Procesa la notificación de actualización de un evento
     */
    private void procesarEventoActualizado(NotificacionEventoDTO notificacion) {
        log.info("Evento actualizado: {} - {}", 
                notificacion.getEventoId(), notificacion.getNombre());
        
        // Sincronizar eventos para actualizar la información
        try {
            eventoSyncService.sincronizarTodos();
            log.info("Sincronización de eventos completada tras actualización");
        } catch (Exception e) {
            log.error("Error al sincronizar eventos tras actualización: {}", e.getMessage());
        }
    }

    /**
     * Procesa la notificación de cancelación de un evento
     */
    private void procesarEventoCancelado(NotificacionEventoDTO notificacion) {
        log.warn("Evento cancelado: {} - {}", 
                notificacion.getEventoId(), notificacion.getNombre());
        
        // TODO: Marcar evento como inactivo en la base de datos
        // TODO: Notificar a usuarios con sesiones activas en ese evento
        // TODO: Cancelar sesiones de compra activas
        
        log.info("Procesamiento de evento cancelado completado");
    }

    /**
     * Procesa la notificación de bloqueo de un asiento
     */
    private void procesarAsientoBloqueado(NotificacionAsientoDTO notificacion) {
        log.debug("Asiento bloqueado: {}:{} en evento {}", 
                notificacion.getFila(), notificacion.getColumna(), 
                notificacion.getEventoId());
        
        // El estado del asiento se consultará desde Redis vía proxy cuando sea necesario
        // No es necesario guardar en base de datos local
        
        // TODO: Opcionalmente, notificar a clientes móviles vía WebSockets
    }

    /**
     * Procesa la notificación de venta de un asiento
     */
    private void procesarAsientoVendido(NotificacionAsientoDTO notificacion) {
        log.info("Asiento vendido: {}:{} en evento {}", 
                notificacion.getFila(), notificacion.getColumna(), 
                notificacion.getEventoId());
        
        // El estado del asiento se consultará desde Redis vía proxy cuando sea necesario
        
        // TODO: Verificar si hay sesiones de compra locales afectadas
        // TODO: Notificar a clientes móviles vía WebSockets
    }

    /**
     * Procesa la notificación de liberación de un asiento
     */
    private void procesarAsientoLiberado(NotificacionAsientoDTO notificacion) {
        log.debug("Asiento liberado: {}:{} en evento {}", 
                notificacion.getFila(), notificacion.getColumna(), 
                notificacion.getEventoId());
        
        // El estado del asiento se consultará desde Redis vía proxy cuando sea necesario
        
        // TODO: Notificar a clientes móviles vía WebSockets
    }
}

