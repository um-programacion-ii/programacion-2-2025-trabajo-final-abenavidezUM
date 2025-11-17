package com.eventos.backend.kafka;

import com.eventos.backend.dto.kafka.EventoKafkaMessageDTO;
import com.eventos.backend.service.EventoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventoKafkaListener {

    private final EventoSyncService eventoSyncService;

    /**
     * Listener para mensajes de cambios en eventos
     * 
     * @param message Mensaje recibido de Kafka
     * @param partition Partición de Kafka
     * @param offset Offset del mensaje
     * @param acknowledgment Para hacer commit manual
     */
    @KafkaListener(
        topics = "${spring.kafka.topic.eventos}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEventoChange(
            @Payload EventoKafkaMessageDTO message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Mensaje Kafka recibido - Partición: {}, Offset: {}, Operación: {}, EventoID: {}", 
                    partition, offset, message.getOperacion(), message.getEventoId());
            
            // Procesar el mensaje según el tipo de operación
            procesarMensaje(message);
            
            // Hacer commit manual del offset solo si el procesamiento fue exitoso
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Offset {} confirmado exitosamente", offset);
            }
            
        } catch (Exception e) {
            log.error("Error al procesar mensaje Kafka - Offset: {}, EventoID: {}, Error: {}", 
                    offset, message.getEventoId(), e.getMessage(), e);
            
            // NO hacer acknowledgment para que Kafka reintente
            // El mensaje volverá a ser procesado según la configuración de retry
        }
    }

    /**
     * Procesa el mensaje según el tipo de operación
     */
    private void procesarMensaje(EventoKafkaMessageDTO message) {
        if (message.getEventoId() == null) {
            log.warn("Mensaje sin eventoId, ignorando: {}", message);
            return;
        }

        String operacion = message.getOperacion() != null ? message.getOperacion().toUpperCase() : "UNKNOWN";
        
        switch (operacion) {
            case "CREATE":
                log.info("Evento CREADO: {}", message.getEventoId());
                sincronizarEvento(message.getEventoId());
                break;
                
            case "UPDATE":
                log.info("Evento ACTUALIZADO: {}", message.getEventoId());
                sincronizarEvento(message.getEventoId());
                break;
                
            case "DELETE":
                log.info("Evento ELIMINADO: {}", message.getEventoId());
                // Al sincronizar, el servicio lo marcará como inactivo si no existe en cátedra
                sincronizarEvento(message.getEventoId());
                break;
                
            default:
                log.warn("Operación desconocida: {} para evento {}", operacion, message.getEventoId());
                // Igual intentamos sincronizar por las dudas
                sincronizarEvento(message.getEventoId());
        }
    }

    /**
     * Sincroniza un evento específico usando el servicio de sincronización
     */
    private void sincronizarEvento(Long eventoId) {
        try {
            log.debug("Iniciando sincronización del evento: {}", eventoId);
            eventoSyncService.sincronizarEvento(eventoId);
            log.info("Evento {} sincronizado exitosamente desde Kafka", eventoId);
        } catch (Exception e) {
            log.error("Error al sincronizar evento {} desde Kafka: {}", eventoId, e.getMessage(), e);
            // Re-lanzar la excepción para que Kafka maneje el reintento
            throw new RuntimeException("Error en sincronización de evento " + eventoId, e);
        }
    }
}

