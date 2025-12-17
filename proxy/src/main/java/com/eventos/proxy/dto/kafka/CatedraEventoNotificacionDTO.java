package com.eventos.proxy.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para notificaciones recibidas de Kafka de cátedra
 * Representa cambios en eventos (nuevo evento, actualización, cancelación)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatedraEventoNotificacionDTO {
    private String tipo; // "NUEVO_EVENTO", "EVENTO_ACTUALIZADO", "EVENTO_CANCELADO"
    private Long eventoId;
    private String nombre;
    private String fecha;
    private String descripcion;
    private String timestamp;
}

