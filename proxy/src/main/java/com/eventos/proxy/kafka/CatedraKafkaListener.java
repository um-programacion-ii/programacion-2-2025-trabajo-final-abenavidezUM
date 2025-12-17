package com.eventos.proxy.kafka;

import com.eventos.proxy.dto.kafka.CatedraAsientoNotificacionDTO;
import com.eventos.proxy.dto.kafka.CatedraEventoNotificacionDTO;
import com.eventos.proxy.service.BackendNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener de Kafka que consume notificaciones del servidor de cátedra
 * y las reenvía al backend para mantener sincronización
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatedraKafkaListener {

    private final BackendNotificationService backendNotificationService;
    private final ObjectMapper objectMapper;

    @Value("${catedra.kafka.topic}")
    private String topic;

    /**
     * Escucha mensajes de cambios en eventos desde Kafka de cátedra
     * 
     * Tipos de mensajes esperados:
     * - NUEVO_EVENTO: Se creó un nuevo evento
     * - EVENTO_ACTUALIZADO: Se modificó un evento existente
     * - EVENTO_CANCELADO: Se canceló un evento
     * - ASIENTO_BLOQUEADO: Se bloqueó un asiento
     * - ASIENTO_VENDIDO: Se vendió un asiento
     * - ASIENTO_LIBERADO: Se liberó un asiento
     */
    @KafkaListener(
        topics = "${catedra.kafka.topic}",
        groupId = "${catedra.kafka.group-id}",
        containerFactory = "catedraKafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Mensaje recibido de Kafka cátedra - Topic: {}, Partition: {}, Offset: {}", 
                receivedTopic, partition, offset);
        log.debug("Contenido del mensaje: {}", message);
        
        try {
            // Intentar parsear como notificación genérica para obtener el tipo
            var jsonNode = objectMapper.readTree(message);
            String tipo = jsonNode.has("tipo") ? jsonNode.get("tipo").asText() : "";
            
            log.info("Procesando notificación de tipo: {}", tipo);
            
            // Procesar según el tipo de notificación
            if (tipo.contains("EVENTO")) {
                // Notificaciones de eventos
                CatedraEventoNotificacionDTO notificacion = 
                        objectMapper.readValue(message, CatedraEventoNotificacionDTO.class);
                procesarNotificacionEvento(notificacion);
            } else if (tipo.contains("ASIENTO")) {
                // Notificaciones de asientos
                CatedraAsientoNotificacionDTO notificacion = 
                        objectMapper.readValue(message, CatedraAsientoNotificacionDTO.class);
                procesarNotificacionAsiento(notificacion);
            } else {
                log.warn("Tipo de notificación desconocido: {}", tipo);
            }
            
            // Confirmar procesamiento del mensaje
            acknowledgment.acknowledge();
            log.debug("Mensaje procesado y confirmado - Offset: {}", offset);
            
        } catch (Exception e) {
            log.error("Error al procesar mensaje de Kafka en offset {}: {}", 
                    offset, e.getMessage(), e);
            // No hacer acknowledge para que Kafka reintente
        }
    }

    /**
     * Procesa notificaciones de cambios en eventos
     */
    private void procesarNotificacionEvento(CatedraEventoNotificacionDTO notificacion) {
        log.info("Notificación de evento recibida: {} - Evento ID: {}", 
                notificacion.getTipo(), notificacion.getEventoId());
        
        try {
            // Reenviar al backend
            backendNotificationService.notificarCambioEvento(notificacion);
            
            log.info("Notificación de evento {} procesada exitosamente", 
                    notificacion.getEventoId());
        } catch (Exception e) {
            log.error("Error al procesar notificación de evento {}: {}", 
                    notificacion.getEventoId(), e.getMessage());
            throw e; // Re-lanzar para no hacer acknowledge
        }
    }

    /**
     * Procesa notificaciones de cambios en asientos
     */
    private void procesarNotificacionAsiento(CatedraAsientoNotificacionDTO notificacion) {
        log.info("Notificación de asiento recibida: {} - Evento: {}, Asiento: {}:{}", 
                notificacion.getTipo(), notificacion.getEventoId(), 
                notificacion.getFila(), notificacion.getColumna());
        
        try {
            // Reenviar al backend
            backendNotificationService.notificarCambioAsiento(notificacion);
            
            log.info("Notificación de asiento {}:{} evento {} procesada exitosamente", 
                    notificacion.getFila(), notificacion.getColumna(), 
                    notificacion.getEventoId());
        } catch (Exception e) {
            log.error("Error al procesar notificación de asiento {}:{} evento {}: {}", 
                    notificacion.getFila(), notificacion.getColumna(), 
                    notificacion.getEventoId(), e.getMessage());
            throw e; // Re-lanzar para no hacer acknowledge
        }
    }
}

