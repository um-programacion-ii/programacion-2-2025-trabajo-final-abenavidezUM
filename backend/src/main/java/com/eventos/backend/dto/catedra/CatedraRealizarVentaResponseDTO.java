package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraRealizarVentaResponseDTO {
    
    private Long eventoId;
    private Long ventaId;
    private String fechaVenta;
    private List<CatedraAsientoDTO> asientos;
    private Boolean resultado;
    private String descripcion;
    private BigDecimal precioVenta;
}

