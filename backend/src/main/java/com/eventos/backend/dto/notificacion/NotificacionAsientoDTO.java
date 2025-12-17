package com.eventos.backend.dto.notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir notificaciones de cambios en asientos desde el proxy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionAsientoDTO {
    private String tipo; // "ASIENTO_BLOQUEADO", "ASIENTO_VENDIDO", "ASIENTO_LIBERADO"
    private Long eventoId;
    private Integer fila;
    private Integer columna;
    private String nuevoEstado; // "LIBRE", "BLOQUEADO", "VENDIDO", "OCUPADO"
    private String timestamp;
}

