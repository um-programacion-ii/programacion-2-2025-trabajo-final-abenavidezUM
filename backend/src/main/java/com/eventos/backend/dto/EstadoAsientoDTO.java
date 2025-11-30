package com.eventos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa el estado de un asiento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoAsientoDTO {

    private Integer fila;
    private Integer columna;
    
    /**
     * Estados posibles:
     * - LIBRE: Disponible para selección
     * - BLOQUEADO: Bloqueado temporalmente por otro usuario
     * - OCUPADO: Vendido
     * - SELECCIONADO: Seleccionado por el usuario actual
     */
    private String estado;
    
    public static final String LIBRE = "LIBRE";
    public static final String BLOQUEADO = "BLOQUEADO";
    public static final String OCUPADO = "OCUPADO";
    public static final String SELECCIONADO = "SELECCIONADO";
}

