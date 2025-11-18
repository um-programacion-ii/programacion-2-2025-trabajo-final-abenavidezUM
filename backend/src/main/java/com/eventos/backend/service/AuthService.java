package com.eventos.backend.service;

import com.eventos.backend.domain.Usuario;
import com.eventos.backend.dto.JwtResponseDTO;
import com.eventos.backend.dto.LoginRequestDTO;
import com.eventos.backend.dto.RegisterRequestDTO;
import com.eventos.backend.exception.ConflictException;
import com.eventos.backend.exception.ResourceNotFoundException;
import com.eventos.backend.repository.UsuarioRepository;
import com.eventos.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RedisService redisService;

    /**
     * Autenticar usuario y generar token JWT
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        // Autenticar credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Obtener información del usuario
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", loginRequest.getUsername()));

        // Guardar token en Redis con TTL de 1 hora
        String redisKey = "auth:token:" + usuario.getId();
        redisService.save(redisKey, jwt, 1, java.util.concurrent.TimeUnit.HOURS);

        log.info("Usuario autenticado exitosamente: {}", loginRequest.getUsername());

        return new JwtResponseDTO(jwt, usuario.getUsername(), usuario.getEmail());
    }

    /**
     * Registrar nuevo usuario
     */
    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO registerRequest) {
        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("El username ya está en uso");
        }

        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está en uso");
        }

        // Crear nuevo usuario
        Usuario usuario = Usuario.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .enabled(true)
                .build();

        usuarioRepository.save(usuario);

        log.info("Nuevo usuario registrado: {}", usuario.getUsername());

        // Autenticar y generar token
        LoginRequestDTO loginRequest = new LoginRequestDTO(
                registerRequest.getUsername(),
                registerRequest.getPassword()
        );

        return login(loginRequest);
    }

    /**
     * Cerrar sesión (invalidar token)
     */
    @Transactional
    public void logout(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

        // Eliminar token de Redis
        String redisKey = "auth:token:" + usuario.getId();
        redisService.delete(redisKey);

        log.info("Usuario cerró sesión: {}", username);
    }

    /**
     * Validar si un token está activo en Redis
     */
    public boolean isTokenActive(Long userId, String token) {
        String redisKey = "auth:token:" + userId;
        String storedToken = redisService.get(redisKey, String.class);
        return token.equals(storedToken);
    }

    /**
     * Obtener usuario actual autenticado
     */
    @Transactional(readOnly = true)
    public Usuario getCurrentUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
    }

    /**
     * Refrescar token JWT
     * Genera un nuevo token a partir de un token válido (aunque esté cerca de expirar)
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO refreshToken(String token) {
        // Validar que el token sea válido
        if (!tokenProvider.validateToken(token)) {
            throw new com.eventos.backend.exception.UnauthorizedException("Token inválido o expirado");
        }

        // Obtener username del token
        String username = tokenProvider.getUsernameFromToken(token);
        
        // Verificar que el usuario existe y está habilitado
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

        if (!usuario.getEnabled()) {
            throw new com.eventos.backend.exception.ForbiddenException("Usuario deshabilitado");
        }

        // Generar nuevo token
        String newToken = tokenProvider.generateTokenFromUsername(username);

        // Actualizar token en Redis con TTL de 1 hora
        String redisKey = "auth:token:" + usuario.getId();
        redisService.save(redisKey, newToken, 1, java.util.concurrent.TimeUnit.HOURS);

        log.info("Token refrescado exitosamente para usuario: {}", username);

        return new JwtResponseDTO(newToken, usuario.getUsername(), usuario.getEmail());
    }
}

