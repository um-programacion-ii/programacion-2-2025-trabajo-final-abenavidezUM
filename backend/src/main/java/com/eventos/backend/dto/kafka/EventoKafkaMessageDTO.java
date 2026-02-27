package com.eventos.backend.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mensajes de Kafka sobre cambios en eventos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoKafkaMessageDTO {

    /**
     * Tipo de operación realizada
     */
    private String operacion; // CREATE, UPDATE, DELETE
    
    /**
     * ID externo del evento afectado
     */
    private Long eventoId;
    
    /**
     * Timestamp del cambio
     */
    private LocalDateTime timestamp;
    
    /**
     * Usuario que realizó el cambio (opcional)
     */
    private String usuario;
    
    /**
     * Descripción del cambio (opcional)
     */
    private String descripcion;
}

