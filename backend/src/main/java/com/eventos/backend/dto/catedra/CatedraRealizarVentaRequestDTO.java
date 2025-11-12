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
public class CatedraRealizarVentaRequestDTO {
    
    private Long eventoId;
    private String fecha;
    private BigDecimal precioVenta;
    private List<CatedraAsientoDTO> asientos;
}

