package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para evento resumido (sin integrantes, dirección, imagen ni dimensiones)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoResumenDTO {
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private Double precioEntrada;
    private EventoTipoDTO eventoTipo;
}

