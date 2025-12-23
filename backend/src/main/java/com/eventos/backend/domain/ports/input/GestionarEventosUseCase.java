package com.eventos.backend.domain.ports.input;

import com.eventos.backend.dto.EventoDetalleDTO;
import com.eventos.backend.dto.EventoResumenDTO;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de gestión de eventos
 * Define las operaciones del dominio para consulta de eventos
 */
public interface GestionarEventosUseCase {
    
    /**
     * Obtiene todos los eventos activos (resumen)
     * @return lista de eventos resumidos
     */
    List<EventoResumenDTO> obtenerEventosActivos();
    
    /**
     * Obtiene los detalles completos de un evento
     * @param id identificador del evento
     * @return detalles del evento
     */
    EventoDetalleDTO obtenerEventoPorId(Long id);
    
    /**
     * Sincroniza eventos desde el servicio de cátedra
     */
    void sincronizarEventos();
}

