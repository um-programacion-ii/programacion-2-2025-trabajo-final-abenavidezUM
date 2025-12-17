package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de consulta de estado de un asiento específico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoAsientoResponseDTO {
    private Long eventoId;
    private Integer fila;
    private Integer columna;
    private String estado; // "LIBRE", "BLOQUEADO", "VENDIDO", "OCUPADO"
    private String timestamp;
}

