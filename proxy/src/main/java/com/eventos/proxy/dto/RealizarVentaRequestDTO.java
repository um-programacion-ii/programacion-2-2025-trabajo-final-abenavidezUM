package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la petición de realizar venta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealizarVentaRequestDTO {
    private Long eventoId;
    private String fecha;
    private Double precioVenta;
    private List<AsientoVentaDTO> asientos;
}

