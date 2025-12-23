package com.eventos.backend.domain.ports.input;

import com.eventos.backend.dto.VentaDTO;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de ventas
 * Define las operaciones del dominio para registro de ventas
 */
public interface RealizarVentaUseCase {
    
    /**
     * Realiza una venta de asientos para un evento
     * Confirma la compra con los asientos de la sesión actual
     * @return venta registrada
     */
    VentaDTO realizarVenta();
    
    /**
     * Obtiene todas las ventas del usuario actual
     * @return lista de ventas
     */
    List<VentaDTO> obtenerVentasUsuario();
    
    /**
     * Obtiene los detalles de una venta específica
     * @param id identificador de la venta
     * @return detalles de la venta
     */
    VentaDTO obtenerVentaPorId(Long id);
}

