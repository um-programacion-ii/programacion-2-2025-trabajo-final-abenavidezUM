package com.eventos.proxy.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para notificaciones de cambios en estado de asientos recibidas de Kafka de cátedra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatedraAsientoNotificacionDTO {
    private String tipo; // "ASIENTO_BLOQUEADO", "ASIENTO_VENDIDO", "ASIENTO_LIBERADO"
    private Long eventoId;
    private Integer fila;
    private Integer columna;
    private String nuevoEstado; // "LIBRE", "BLOQUEADO", "VENDIDO", "OCUPADO"
    private String timestamp;
}

