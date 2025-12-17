package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un asiento con su estado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsientoEstadoDTO {
    private Integer fila;
    private Integer columna;
    private String estado; // "Bloqueo exitoso", "Ocupado", "Bloqueado", "Libre"
}

