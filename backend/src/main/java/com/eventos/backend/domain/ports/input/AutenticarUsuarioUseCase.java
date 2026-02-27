package com.eventos.backend.domain.ports.input;

import com.eventos.backend.dto.JwtResponseDTO;
import com.eventos.backend.dto.LoginRequestDTO;
import com.eventos.backend.dto.RegisterRequestDTO;

/**
 * Puerto de entrada para casos de uso de autenticación
 * Define las operaciones del dominio para autenticación de usuarios
 */
public interface AutenticarUsuarioUseCase {
    
    /**
     * Registra un nuevo usuario en el sistema
     * @param request datos del nuevo usuario
     * @return respuesta con token JWT
     */
    JwtResponseDTO registrarUsuario(RegisterRequestDTO request);
    
    /**
     * Autentica un usuario existente
     * @param request credenciales del usuario
     * @return respuesta con token JWT
     */
    JwtResponseDTO autenticar(LoginRequestDTO request);
}

