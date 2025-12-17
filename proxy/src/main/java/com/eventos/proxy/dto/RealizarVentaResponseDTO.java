package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de realizar venta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealizarVentaResponseDTO {
    private Long eventoId;
    private Long ventaId;
    private String fechaVenta;
    private Boolean resultado;
    private String descripcion;
    private Double precioVenta;
    private List<AsientoVentaDTO> asientos;
}

