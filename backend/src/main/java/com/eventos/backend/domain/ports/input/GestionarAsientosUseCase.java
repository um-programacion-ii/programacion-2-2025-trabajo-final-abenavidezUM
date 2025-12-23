package com.eventos.backend.domain.ports.input;

import com.eventos.backend.dto.AsientoSeleccionadoDTO;
import com.eventos.backend.dto.BloquearAsientosResponseDTO;
import com.eventos.backend.dto.MapaAsientosDTO;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de gestión de asientos
 * Define las operaciones del dominio para consulta y bloqueo de asientos
 */
public interface GestionarAsientosUseCase {
    
    /**
     * Obtiene el mapa completo de asientos de un evento
     * @param eventoId identificador del evento
     * @return mapa con estado de todos los asientos
     */
    MapaAsientosDTO obtenerMapaAsientos(Long eventoId);
    
    /**
     * Bloquea asientos temporalmente para una compra
     * @param eventoId identificador del evento
     * @param asientos lista de asientos a bloquear
     * @return resultado del bloqueo
     */
    BloquearAsientosResponseDTO bloquearAsientos(Long eventoId, List<AsientoSeleccionadoDTO> asientos);
    
    /**
     * Libera los asientos bloqueados del usuario actual
     */
    void liberarAsientos();
}

