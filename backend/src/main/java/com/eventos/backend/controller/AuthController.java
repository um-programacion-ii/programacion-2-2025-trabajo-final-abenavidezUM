package com.eventos.backend.controller;

import com.eventos.backend.domain.Usuario;
import com.eventos.backend.dto.JwtResponseDTO;
import com.eventos.backend.dto.LoginRequestDTO;
import com.eventos.backend.dto.RefreshTokenRequestDTO;
import com.eventos.backend.dto.RegisterRequestDTO;
import com.eventos.backend.dto.UsuarioDTO;
import com.eventos.backend.mapper.UsuarioMapper;
import com.eventos.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticación y gestión de sesiones
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para registro, login, logout y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con username y password, retorna un token JWT válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());
        JwtResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario y retorna un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro exitoso",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o usuario ya existe"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Intento de registro para usuario: {}", registerRequest.getUsername());
        JwtResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el token JWT del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
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

    @Operation(
            summary = "Refrescar token JWT",
            description = "Genera un nuevo token JWT a partir de un token válido existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        log.info("Intento de refresh token");
        JwtResponseDTO response = authService.refreshToken(refreshRequest.getToken());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener usuario actual",
            description = "Retorna la información del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Usuario usuario = authService.getCurrentUser(username);
            UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);
            return ResponseEntity.ok(usuarioDTO);
        }
        return ResponseEntity.status(401).build();
    }
}
