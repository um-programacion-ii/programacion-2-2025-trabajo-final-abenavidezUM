package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraRealizarVentaRequestDTO {
    
    private Long eventoId;
    private String fecha;
    private Double precioVenta;  // ✅ Cambio de BigDecimal a Double
    private List<CatedraAsientoDTO> asientos;
}

