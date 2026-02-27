package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraEventoResumenDTO {
    
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private BigDecimal precioEntrada;
    private CatedraTipoEventoDTO eventoTipo;
}

