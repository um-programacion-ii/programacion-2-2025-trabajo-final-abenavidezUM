package com.eventos.backend.infrastructure.adapter.input.rest;

import com.eventos.backend.dto.ChangePasswordRequestDTO;
import com.eventos.backend.dto.UpdateUsuarioRequestDTO;
import com.eventos.backend.dto.UsuarioDTO;
import com.eventos.backend.infrastructure.adapter.output.external.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios
     * Obtener todos los usuarios (requiere autenticación)
     */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        log.info("Obteniendo todos los usuarios");
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * GET /api/usuarios/{id}
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        log.info("Obteniendo usuario con ID: {}", id);
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * GET /api/usuarios/username/{username}
     * Obtener usuario por username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioDTO> getUsuarioByUsername(@PathVariable String username) {
        log.info("Obteniendo usuario con username: {}", username);
        UsuarioDTO usuario = usuarioService.findByUsername(username);
        return ResponseEntity.ok(usuario);
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualizar información de usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> updateUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequestDTO updateRequest) {
        log.info("Actualizando usuario con ID: {}", id);
        UsuarioDTO usuario = usuarioService.update(id, updateRequest);
        return ResponseEntity.ok(usuario);
    }

    /**
     * PUT /api/usuarios/{id}/password
     * Cambiar contraseña de usuario
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDTO changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;
        
        log.info("Cambiando contraseña para usuario con ID: {}", id);
        usuarioService.changePassword(id, changePasswordRequest, currentUsername);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/usuarios/{id}/enable
     * Habilitar usuario
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableUsuario(@PathVariable Long id) {
        log.info("Habilitando usuario con ID: {}", id);
        usuarioService.enable(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/usuarios/{id}/disable
     * Deshabilitar usuario
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableUsuario(@PathVariable Long id) {
        log.info("Deshabilitando usuario con ID: {}", id);
        usuarioService.disable(id);
        return ResponseEntity.ok().build();
    }
}

