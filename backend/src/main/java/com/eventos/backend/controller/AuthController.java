package com.eventos.backend.controller;

import com.eventos.backend.dto.JwtResponseDTO;
import com.eventos.backend.dto.LoginRequestDTO;
import com.eventos.backend.dto.RegisterRequestDTO;
import com.eventos.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Autenticar usuario y obtener token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());
        JwtResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Registrar nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Intento de registro para usuario: {}", registerRequest.getUsername());
        JwtResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Cerrar sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            authService.logout(username);
            log.info("Logout exitoso para usuario: {}", username);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/auth/me
     * Obtener información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Usuario autenticado: " + authentication.getName());
        }
        return ResponseEntity.ok("No autenticado");
    }
}

