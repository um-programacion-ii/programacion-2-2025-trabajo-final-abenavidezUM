package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para un asiento en una venta (con datos de la persona)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsientoVentaDTO {
    private Integer fila;
    private Integer columna;
    private String persona;
    private String estado; // "Vendido", "Libre", "Ocupado"
}

