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
public class CatedraVentaResumenDTO {
    
    private Long eventoId;
    private Long ventaId;
    private String fechaVenta;
    private Boolean resultado;
    private String descripcion;
    private BigDecimal precioVenta;
    private Integer cantidadAsientos;
}

