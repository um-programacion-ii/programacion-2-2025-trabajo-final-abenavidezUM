package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraAsientoDTO {
    
    private Integer fila;
    private Integer columna;
    private String persona;
    private String estado;
}

