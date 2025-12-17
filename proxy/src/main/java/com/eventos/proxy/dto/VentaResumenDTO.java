package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resumen de venta (listado)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaResumenDTO {
    private Long eventoId;
    private Long ventaId;
    private String fechaVenta;
    private Boolean resultado;
    private String descripcion;
    private Double precioVenta;
    private Integer cantidadAsientos;
}

