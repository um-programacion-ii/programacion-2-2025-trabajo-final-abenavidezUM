package com.eventos.backend.domain.ports.output;

import com.eventos.backend.domain.model.Venta;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de ventas
 * Define las operaciones de persistencia que el dominio necesita
 */
public interface VentaRepositoryPort {
    
    /**
     * Guarda o actualiza una venta
     * @param venta venta a guardar
     * @return venta guardada
     */
    Venta save(Venta venta);
    
    /**
     * Busca una venta por su ID
     * @param id identificador de la venta
     * @return venta si existe
     */
    Optional<Venta> findById(Long id);
    
    /**
     * Obtiene todas las ventas de un usuario
     * @param usuarioId ID del usuario
     * @return lista de ventas
     */
    List<Venta> findByUsuarioId(Long usuarioId);
    
    /**
     * Obtiene todas las ventas pendientes de confirmación
     * @return lista de ventas pendientes
     */
    List<Venta> findPendientesConfirmacion();
}

