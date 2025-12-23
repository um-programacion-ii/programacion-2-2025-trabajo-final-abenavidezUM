package com.eventos.backend.domain.ports.output;

import com.eventos.backend.dto.proxy.ProxyEstadoAsientoResponseDTO;
import com.eventos.backend.dto.proxy.ProxyMapaAsientosResponseDTO;

/**
 * Puerto de salida para comunicación con el servicio Proxy
 * Define las operaciones de comunicación externa que el dominio necesita
 */
public interface ProxyApiPort {
    
    /**
     * Obtiene el estado de un asiento específico desde Redis de Cátedra
     * @param eventoId ID del evento
     * @param fila fila del asiento
     * @param columna columna del asiento
     * @return estado del asiento
     */
    ProxyEstadoAsientoResponseDTO obtenerEstadoAsiento(Long eventoId, Integer fila, Integer columna);
    
    /**
     * Obtiene el mapa completo de asientos desde Redis de Cátedra
     * @param eventoId ID del evento
     * @return mapa de asientos
     */
    ProxyMapaAsientosResponseDTO obtenerMapaAsientos(Long eventoId);
    
    /**
     * Verifica si el proxy está disponible
     * @return true si está disponible
     */
    boolean isProxyAvailable();
}

