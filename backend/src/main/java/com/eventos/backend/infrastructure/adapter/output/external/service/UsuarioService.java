package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.eventos.backend.domain.model.Usuario;
import com.eventos.backend.dto.ChangePasswordRequestDTO;
import com.eventos.backend.dto.UpdateUsuarioRequestDTO;
import com.eventos.backend.dto.UsuarioDTO;
import com.eventos.backend.domain.exception.BadRequestException;
import com.eventos.backend.domain.exception.ConflictException;
import com.eventos.backend.domain.exception.ForbiddenException;
import com.eventos.backend.domain.exception.ResourceNotFoundException;
import com.eventos.backend.infrastructure.mapper.UsuarioMapper;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtener todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        log.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        log.debug("Obteniendo usuario con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id.toString()));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Obtener usuario por username
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findByUsername(String username) {
        log.debug("Obteniendo usuario con username: {}", username);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        log.debug("Obteniendo usuario con email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Actualizar información de usuario
     */
    @Transactional
    public UsuarioDTO update(Long id, UpdateUsuarioRequestDTO updateRequest) {
        log.debug("Actualizando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id.toString()));

        // Validar que el email no esté en uso por otro usuario
        if (!usuario.getEmail().equals(updateRequest.getEmail()) && 
            usuarioRepository.existsByEmail(updateRequest.getEmail())) {
            throw new ConflictException("El email ya está en uso");
        }

        // Actualizar campos
        usuario.setFirstName(updateRequest.getFirstName());
        usuario.setLastName(updateRequest.getLastName());
        usuario.setEmail(updateRequest.getEmail());

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", updatedUsuario.getUsername());

        return usuarioMapper.toDTO(updatedUsuario);
    }

    /**
     * Cambiar contraseña de usuario
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDTO changePasswordRequest, String currentUsername) {
        log.debug("Cambiando contraseña para usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id.toString()));

        // Verificar que el usuario solo pueda cambiar su propia contraseña
        if (!usuario.getUsername().equals(currentUsername)) {
            throw new ForbiddenException("No tienes permiso para cambiar la contraseña de otro usuario");
        }

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), usuario.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada exitosamente para usuario: {}", usuario.getUsername());
    }

    /**
     * Deshabilitar usuario (soft delete)
     */
    @Transactional
    public void disable(Long id) {
        log.debug("Deshabilitando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id.toString()));

        usuario.setEnabled(false);
        usuarioRepository.save(usuario);

        log.info("Usuario deshabilitado: {}", usuario.getUsername());
    }

    /**
     * Habilitar usuario
     */
    @Transactional
    public void enable(Long id) {
        log.debug("Habilitando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id.toString()));

        usuario.setEnabled(true);
        usuarioRepository.save(usuario);

        log.info("Usuario habilitado: {}", usuario.getUsername());
    }

    /**
     * Verificar si un username existe
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Verificar si un email existe
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}

