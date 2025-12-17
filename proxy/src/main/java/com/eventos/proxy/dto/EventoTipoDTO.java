package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el tipo de evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoTipoDTO {
    private String nombre;
    private String descripcion;
}

