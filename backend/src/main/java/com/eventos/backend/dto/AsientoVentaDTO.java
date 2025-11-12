package com.eventos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsientoVentaDTO {

    private Long id;
    private Integer fila;
    private Integer columna;
    private String nombrePersona;
    private String estado;
}

