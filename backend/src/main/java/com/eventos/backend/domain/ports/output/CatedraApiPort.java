package com.eventos.backend.domain.ports.output;

import com.eventos.backend.dto.catedra.*;

import java.util.List;

/**
 * Puerto de salida para comunicación con el API de Cátedra
 * Define las operaciones de comunicación externa que el dominio necesita
 */
public interface CatedraApiPort {
    
    /**
     * Obtiene todos los eventos resumidos desde Cátedra
     * @return lista de eventos resumidos
     */
    List<CatedraEventoResumenDTO> obtenerEventosResumidos();
    
    /**
     * Obtiene todos los eventos completos desde Cátedra
     * @return lista de eventos completos
     */
    List<CatedraEventoCompletoDTO> obtenerEventosCompletos();
    
    /**
     * Obtiene un evento específico por ID desde Cátedra
     * @param id identificador del evento
     * @return evento completo
     */
    CatedraEventoCompletoDTO obtenerEventoPorId(Long id);
    
    /**
     * Bloquea asientos en Cátedra
     * @param request solicitud de bloqueo
     * @return respuesta del bloqueo
     */
    CatedraBloquearAsientosResponseDTO bloquearAsientos(CatedraBloquearAsientosRequestDTO request);
    
    /**
     * Realiza una venta en Cátedra
     * @param request solicitud de venta
     * @return respuesta de la venta
     */
    CatedraRealizarVentaResponseDTO realizarVenta(CatedraRealizarVentaRequestDTO request);
    
    /**
     * Obtiene todas las ventas del usuario desde Cátedra
     * @return lista de ventas resumidas
     */
    List<CatedraVentaResumenDTO> obtenerVentas();
    
    /**
     * Obtiene una venta específica por ID desde Cátedra
     * @param id identificador de la venta
     * @return venta detallada
     */
    CatedraVentaDetalleDTO obtenerVentaPorId(Long id);
}

