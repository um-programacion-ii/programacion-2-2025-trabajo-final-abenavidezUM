package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un asiento (fila y columna)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsientoDTO {
    private Integer fila;
    private Integer columna;
}

