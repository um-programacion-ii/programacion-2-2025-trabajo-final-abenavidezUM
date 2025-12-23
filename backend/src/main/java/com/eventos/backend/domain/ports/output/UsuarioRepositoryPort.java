package com.eventos.backend.domain.ports.output;

import com.eventos.backend.domain.model.Usuario;

import java.util.Optional;

/**
 * Puerto de salida para persistencia de usuarios
 * Define las operaciones de persistencia que el dominio necesita
 */
public interface UsuarioRepositoryPort {
    
    /**
     * Guarda o actualiza un usuario
     * @param usuario usuario a guardar
     * @return usuario guardado
     */
    Usuario save(Usuario usuario);
    
    /**
     * Busca un usuario por su ID
     * @param id identificador del usuario
     * @return usuario si existe
     */
    Optional<Usuario> findById(Long id);
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username nombre de usuario
     * @return usuario si existe
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por su email
     * @param email email del usuario
     * @return usuario si existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el username dado
     * @param username nombre de usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email email del usuario
     * @return true si existe
     */
    boolean existsByEmail(String email);
}

