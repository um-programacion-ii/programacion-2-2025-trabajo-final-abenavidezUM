package com.eventos.backend.dto.notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir notificaciones de cambios en eventos desde el proxy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEventoDTO {
    private String tipo; // "NUEVO_EVENTO", "EVENTO_ACTUALIZADO", "EVENTO_CANCELADO"
    private Long eventoId;
    private String nombre;
    private String fecha;
    private String descripcion;
    private String timestamp;
}

