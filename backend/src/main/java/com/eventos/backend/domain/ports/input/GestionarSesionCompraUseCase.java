package com.eventos.backend.domain.ports.input;

import com.eventos.backend.dto.AsientoSeleccionadoDTO;
import com.eventos.backend.dto.IniciarSesionRequestDTO;
import com.eventos.backend.dto.PersonaAsientoDTO;
import com.eventos.backend.dto.SesionCompraDTO;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de gestión de sesión de compra
 * Define las operaciones del dominio para el proceso de compra
 */
public interface GestionarSesionCompraUseCase {
    
    /**
     * Inicia una nueva sesión de compra para un evento
     * @param request datos de inicio de sesión
     * @return sesión de compra creada
     */
    SesionCompraDTO iniciarSesion(IniciarSesionRequestDTO request);
    
    /**
     * Obtiene la sesión de compra activa del usuario actual
     * @return sesión de compra o null si no existe
     */
    SesionCompraDTO obtenerSesionActual();
    
    /**
     * Actualiza los asientos seleccionados en la sesión
     * @param asientos lista de asientos seleccionados
     * @return sesión actualizada
     */
    SesionCompraDTO actualizarAsientos(List<AsientoSeleccionadoDTO> asientos);
    
    /**
     * Actualiza los datos de las personas para cada asiento
     * @param personas lista de personas por asiento
     * @return sesión actualizada
     */
    SesionCompraDTO actualizarPersonas(List<PersonaAsientoDTO> personas);
    
    /**
     * Marca los asientos como bloqueados en la sesión
     * @return sesión actualizada
     */
    SesionCompraDTO marcarAsientosBloqueados();
    
    /**
     * Limpia la sesión de compra actual
     */
    void limpiarSesion();
}

